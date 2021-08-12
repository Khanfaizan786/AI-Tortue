package com.example.a_i_tortue.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a_i_tortue.R
import com.example.a_i_tortue.adapter.DisplayParticipantsAdapter
import com.example.a_i_tortue.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.iceteck.silicompressorr.FileUtils
import com.iceteck.silicompressorr.SiliCompressor
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File

class GroupInfoDisplayActivity : AppCompatActivity() {

    private val CAMERA_REQUEST_CODE = 100
    private val STORAGE_REQUEST_CODE = 200

    private lateinit var cameraPermissions: Array<String>
    private lateinit var storagePermissions: Array<String>

    private var resultUri: Uri? = null

    private lateinit var toolbarGroupInfoDisplay:Toolbar
    private lateinit var imgGroupImageDisplay:ImageView
    private lateinit var imgBtnEditGroupIcon:ImageButton
    private lateinit var txtEditableGroupName:TextView
    private lateinit var imgBtnEditGroupName:ImageButton
    private lateinit var recyclerGroupParticipants:RecyclerView
    private lateinit var btnAddParticipants:Button
    private lateinit var btnExitGroup:Button
    private lateinit var progressBarGroupDisplay:ProgressBar
    private var groupName: String?="Group Name"
    private var groupKey:String?="Group Key"
    private var groupImage:String?="none"
    private var admin:String?="admin"
    private var participantsList: ArrayList<String>?=null
    private var usersList= arrayListOf<Users>()
    private lateinit var usersRef:DatabaseReference
    private lateinit var groupsRef:DatabaseReference
    private lateinit var displayParticipantsAdapter: DisplayParticipantsAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserId:String
    private lateinit var downloadUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_info_display)

        toolbarGroupInfoDisplay=findViewById(R.id.toolbarGroupInfoDisplay)
        imgGroupImageDisplay=findViewById(R.id.imgGroupImageDisplay)
        imgBtnEditGroupIcon=findViewById(R.id.imgBtnEditGroupIcon)
        txtEditableGroupName=findViewById(R.id.txtEditableGroupName)
        imgBtnEditGroupName=findViewById(R.id.imgBtnEditGroupName)
        recyclerGroupParticipants=findViewById(R.id.recyclerGroupParticipants)
        btnAddParticipants=findViewById(R.id.btnAddParticipants)
        btnExitGroup=findViewById(R.id.btnExitGroup)
        progressBarGroupDisplay=findViewById(R.id.progressBarGroupDisplay)

        layoutManager=LinearLayoutManager(this)

        if (intent!=null) {
            groupName=intent.getStringExtra("groupName")
            groupImage=intent.getStringExtra("groupIcon")
            groupKey=intent.getStringExtra("groupKey")
            participantsList=intent.getStringArrayListExtra("participantsList")
            admin=intent.getStringExtra("admin")
        }

        setSupportActionBar(toolbarGroupInfoDisplay)
        supportActionBar?.title=groupName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        txtEditableGroupName.text=groupName

        if (groupImage!="none") {
            try {
                Picasso.with(this).load(Uri.parse(groupImage)).placeholder(R.drawable.profile)
                    .into(imgGroupImageDisplay)
            } catch (e: Exception) {
            }
        }

        mAuth= FirebaseAuth.getInstance()
        currentUserId=mAuth.currentUser?.uid.toString()
        usersRef=FirebaseDatabase.getInstance().reference.child("Users")
        groupsRef=FirebaseDatabase.getInstance().reference.child("Groups").child(groupKey!!)

        if (currentUserId==admin) {
            btnAddParticipants.visibility=View.VISIBLE
        }

        getAllParticipants()

        cameraPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        storagePermissions =
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

        btnAddParticipants.setOnClickListener {
            if (currentUserId==admin) {
                val intent=Intent(this@GroupInfoDisplayActivity,AddParticipantsActivity::class.java)
                intent.putExtra("groupKey",groupKey)
                intent.putExtra("membersList",participantsList)
                startActivity(intent)
            }
        }

        btnExitGroup.setOnClickListener {
            val builder= AlertDialog.Builder(this@GroupInfoDisplayActivity)
            builder.setMessage("Exit Group ?")
            builder.setPositiveButton("Exit") { _,_->
                exitGroup()
            }
            builder.setNegativeButton("Cancel") { _,_-> }
            builder.create().show()
        }

        imgBtnEditGroupIcon.setOnClickListener {
            pickFromGallery()
        }

        imgBtnEditGroupName.setOnClickListener {
            val builder=AlertDialog.Builder(this@GroupInfoDisplayActivity)
            val inputField=EditText(this@GroupInfoDisplayActivity)
            inputField.setText(groupName)
            builder.setTitle("Change group name")
            builder.setView(inputField)
            builder.setPositiveButton("Save"){ _,_->
                val newName=inputField.text.toString()
                if (!TextUtils.isEmpty(newName)) {
                    updateGroupName(newName)
                }
            }
            builder.setNegativeButton("Cancel"){ _,_-> }
            builder.create().show()
        }
    }

    private fun updateGroupName(newName: String) {
        groupsRef.child("name").setValue(newName).addOnSuccessListener {
            Toast.makeText(
                this@GroupInfoDisplayActivity,
                "Group Name changed successfully",
                Toast.LENGTH_SHORT
            ).show()
            txtEditableGroupName.text=newName
            sendUserToMessageActivity()
        }
    }

    private fun exitGroup() {

        val groupRef=FirebaseDatabase.getInstance().reference.child("Groups").child(groupKey!!)
        val groupUsersRef=FirebaseDatabase.getInstance().reference.child("Group Users").child(groupKey!!)
        val userGroupsRef=FirebaseDatabase.getInstance().reference.child("User Groups").child(currentUserId)

        if (currentUserId==admin) {
            if (participantsList?.size!!>1) {
                if (participantsList!![0]==admin) {
                    groupRef.child("admin").setValue(participantsList!![1])
                } else {
                    groupRef.child("admin").setValue(participantsList!![0])
                }
                groupUsersRef.child(currentUserId).removeValue()
                userGroupsRef.child(groupKey!!).removeValue()
                Toast.makeText(this@GroupInfoDisplayActivity,"Group removed",Toast.LENGTH_SHORT).show()
                sendUserToMessageActivity()
            } else {
                groupUsersRef.child(currentUserId).removeValue()
                userGroupsRef.child(groupKey!!).removeValue()
                Toast.makeText(this@GroupInfoDisplayActivity,"Group removed",Toast.LENGTH_SHORT).show()
                sendUserToMessageActivity()
            }
        } else {
            groupUsersRef.child(currentUserId).removeValue()
            userGroupsRef.child(groupKey!!).removeValue()
            Toast.makeText(this@GroupInfoDisplayActivity,"Group removed",Toast.LENGTH_SHORT).show()
            sendUserToMessageActivity()
        }
    }

    private fun getAllParticipants() {
        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChildren()) {
                    usersList.clear()
                    for (ds in snapshot.children) {
                        val user:Users?=ds.getValue(Users::class.java)
                        if (user!=null && participantsList?.contains(user.uid)!!) {
                            usersList.add(user)
                        }
                        displayParticipantsAdapter = DisplayParticipantsAdapter(this@GroupInfoDisplayActivity,usersList,admin,currentUserId,groupKey,participantsList)
                        displayParticipantsAdapter.notifyDataSetChanged()
                        recyclerGroupParticipants.adapter=displayParticipantsAdapter
                        recyclerGroupParticipants.layoutManager=layoutManager
                    }
                }
            }
        })
    }

    private fun sendUserToMessageActivity() {
        val intent=Intent(this@GroupInfoDisplayActivity,MessageActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("from","GroupInfoActivity")
        startActivity(intent)
        finish()
    }

    private fun pickFromGallery() {
        if (!checkCameraPermission()) {
            requestCameraPermission()
            if (!checkStoragePermission()) {
                requestStoragePermission()
            }
        } else if (!checkStoragePermission()) {
            requestStoragePermission()
        } else {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this@GroupInfoDisplayActivity)
        }
    }

    private fun checkCameraPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this@GroupInfoDisplayActivity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val result1 = ContextCompat.checkSelfPermission(
            this@GroupInfoDisplayActivity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        return result && result1
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this@GroupInfoDisplayActivity,
            cameraPermissions,
            CAMERA_REQUEST_CODE
        )
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this@GroupInfoDisplayActivity,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this@GroupInfoDisplayActivity,
            storagePermissions,
            STORAGE_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    val cameraAccepted =
                        grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted =
                        grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if (cameraAccepted && storageAccepted) {
                        pickFromGallery()
                    } else {
                        Toast.makeText(
                            this@GroupInfoDisplayActivity,
                            "Camera & Storage both permissions are necessary",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    val writeStorageAccepted =
                        grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (writeStorageAccepted) {
                        pickFromGallery()
                    } else {
                        Toast.makeText(
                            this@GroupInfoDisplayActivity,
                            "Storage permissions are necessary",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                resultUri = result.uri
                if (resultUri!=null) {
                    progressBarGroupDisplay.visibility=View.VISIBLE
                    compressImage(resultUri!!)
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val exception = result.error
                Toast.makeText(this, "Error occured: $exception", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun compressImage(
        resultUri: Uri
    ) {
        val file: File = File(
            SiliCompressor.with(this)
                .compress(
                    FileUtils.getPath(this,resultUri), File(this.cacheDir,"temp")
                ))
        val uri=Uri.fromFile(file)
        saveImageToStorage(uri)
    }

    private fun saveImageToStorage(uri: Uri) {
        val groupStorageRef=FirebaseStorage.getInstance().reference.child("Group Icons")
        val iconPath=groupStorageRef.child("$groupKey")
        iconPath.putFile(uri).addOnSuccessListener {
            iconPath.downloadUrl.addOnSuccessListener {
                downloadUrl = it.toString()
                saveGroupInfoToDatabase(downloadUrl)
            }.addOnFailureListener {
                val message= it.message
                Toast.makeText(this@GroupInfoDisplayActivity, "Error occured: $message", Toast.LENGTH_SHORT).show()
                progressBarGroupDisplay.visibility=View.GONE
            }
        }.addOnFailureListener {
            val message= it.message
            Toast.makeText(this@GroupInfoDisplayActivity, "Error occured: $message", Toast.LENGTH_SHORT).show()
            progressBarGroupDisplay.visibility=View.GONE
        }
    }

    private fun saveGroupInfoToDatabase(downloadUrl: String) {
        groupsRef.child("image").setValue(downloadUrl).addOnSuccessListener {
            Toast.makeText(
                this@GroupInfoDisplayActivity,
                "Icon changed successfully",
                Toast.LENGTH_SHORT
            ).show()
            progressBarGroupDisplay.visibility=View.GONE
            Picasso.with(this@GroupInfoDisplayActivity).load(resultUri)
                .placeholder(R.drawable.select_image)
                .into(imgGroupImageDisplay)
            sendUserToMessageActivity()
        }.addOnFailureListener {
            val message= it.message
            Toast.makeText(this@GroupInfoDisplayActivity, "Error occured: $message", Toast.LENGTH_SHORT).show()
            progressBarGroupDisplay.visibility=View.GONE
        }
    }
}