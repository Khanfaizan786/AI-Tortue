package com.example.a_i_tortue.activity

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a_i_tortue.R
import com.example.a_i_tortue.adapter.ParticipantsAdapter
import com.example.a_i_tortue.model.Users
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.iceteck.silicompressorr.FileUtils
import com.iceteck.silicompressorr.SiliCompressor
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class GroupInfoActivity : AppCompatActivity() {

    private val CAMERA_REQUEST_CODE = 100
    private val STORAGE_REQUEST_CODE = 200

    private lateinit var cameraPermissions: Array<String>
    private lateinit var storagePermissions: Array<String>
    private lateinit var loadingBar: ProgressDialog

    private var resultUri: Uri? = null

    private lateinit var saveCurrentDate: String
    private lateinit var saveCurrentTime: String
    private lateinit var postRandomName: String
    private lateinit var downloadUrl: String
    private lateinit var currentUserId: String
    private lateinit var mAuth: FirebaseAuth

    private lateinit var groupsRef: DatabaseReference
    private lateinit var userGroupsRef: DatabaseReference
    private lateinit var usersRef: DatabaseReference
    private lateinit var groupStorageRef: StorageReference

    private lateinit var toolbarGroupInfo:Toolbar
    private lateinit var groupInfoIcon:CircleImageView
    private lateinit var etGroupInfoName:EditText
    private lateinit var btnCreateGroupFinal:FloatingActionButton
    private lateinit var txtCountParticipants:TextView
    private lateinit var recyclerParticipants:RecyclerView
    private var userList= arrayListOf<Users>()
    private var participantsList: ArrayList<String>?=null
    private lateinit var participantsAdapter: ParticipantsAdapter
    private lateinit var allUsersDatabaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_info)

        toolbarGroupInfo=findViewById(R.id.toolbarGroupInfo)
        setSupportActionBar(toolbarGroupInfo)
        supportActionBar?.title="New Group"
        supportActionBar?.subtitle="Add subject"

        groupInfoIcon=findViewById(R.id.groupInfoIcon)
        etGroupInfoName=findViewById(R.id.etGroupInfoName)
        btnCreateGroupFinal=findViewById(R.id.btnCreateGroupFinal)
        txtCountParticipants=findViewById(R.id.txtCountParticipants)
        recyclerParticipants=findViewById(R.id.recyclerParticipants)

        recyclerParticipants.setHasFixedSize(true)
        recyclerParticipants.layoutManager=LinearLayoutManager(this@GroupInfoActivity)

        allUsersDatabaseRef = FirebaseDatabase.getInstance().reference.child("Users")
        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth.currentUser?.uid.toString()
        groupsRef = FirebaseDatabase.getInstance().reference.child("Groups")
        usersRef = FirebaseDatabase.getInstance().reference.child("Group Users")
        userGroupsRef = FirebaseDatabase.getInstance().reference.child("User Groups")
        groupStorageRef = FirebaseStorage.getInstance().reference.child("Group Icons")

        loadingBar= ProgressDialog(this@GroupInfoActivity)

        cameraPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        storagePermissions =
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)


        if (intent!=null) {
            participantsList=intent.getStringArrayListExtra("participants")
            if (!participantsList?.contains(currentUserId)!!) {
                participantsList?.add(currentUserId)
            }
            getAllUsers(participantsList)
            val countParticipants="Total Participants: ${participantsList?.size}"
            txtCountParticipants.text=countParticipants
        }

        groupInfoIcon.setOnClickListener {
            pickFromGallery()
        }

        btnCreateGroupFinal.setOnClickListener {

            if (participantsList!=null) {
                val callForDate = Calendar.getInstance()
                val currentDate = SimpleDateFormat("yyyy-MM-dd")
                saveCurrentDate = currentDate.format(callForDate.time)

                val callForTime = Calendar.getInstance()
                val currentTime = SimpleDateFormat("HH:mm:ss")
                saveCurrentTime = currentTime.format(callForTime.time)

                postRandomName = saveCurrentDate + saveCurrentTime

                val groupName=etGroupInfoName.text.toString()
                val groupImage:String
                if (TextUtils.isEmpty(groupName)) {
                    Toast.makeText(
                        this@GroupInfoActivity,
                        "Please enter subject",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (resultUri==null){
                    groupImage="none"
                    loadingBar.setTitle("Creating Group")
                    loadingBar.setMessage("Please wait...!!")
                    loadingBar.setCanceledOnTouchOutside(true)
                    loadingBar.show()
                    saveGroupInfoToDatabase(groupImage,groupName)
                } else {
                    loadingBar.setTitle("Creating Group")
                    loadingBar.setMessage("Please wait...!!")
                    loadingBar.setCanceledOnTouchOutside(true)
                    loadingBar.show()
                    compressImage(resultUri!!,groupName,postRandomName)
                }
            }
        }
    }

    private fun getAllUsers(participantsList: ArrayList<String>?) {
        allUsersDatabaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList.clear()
                for (ds in dataSnapshot.children) {
                    if (dataSnapshot.exists()) {
                        val users: Users? = ds.getValue<Users>(Users::class.java)
                        if (participantsList!!.contains(users!!.uid) ) {
                            userList.add(users)
                        }
                    }
                }
                participantsAdapter = ParticipantsAdapter(this@GroupInfoActivity, userList)
                recyclerParticipants.adapter = participantsAdapter
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
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
                .start(this@GroupInfoActivity)
        }
    }

    private fun checkCameraPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this@GroupInfoActivity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val result1 = ContextCompat.checkSelfPermission(
            this@GroupInfoActivity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        return result && result1
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this@GroupInfoActivity,
            cameraPermissions,
            CAMERA_REQUEST_CODE
        )
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this@GroupInfoActivity,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this@GroupInfoActivity,
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
                            this@GroupInfoActivity,
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
                            this@GroupInfoActivity,
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
                Picasso.with(this@GroupInfoActivity).load(resultUri)
                    .placeholder(R.drawable.select_image)
                    .into(groupInfoIcon)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val exception = result.error
                Toast.makeText(this, "Error occured: $exception", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun compressImage(
        resultUri: Uri,
        groupName: String,
        postRandomName: String
    ) {
        val file: File = File(
            SiliCompressor.with(this)
                .compress(
                    FileUtils.getPath(this,resultUri), File(this.cacheDir,"temp")
                ))
        val uri=Uri.fromFile(file)
        saveImageToStorage(uri,groupName,postRandomName)
    }

    private fun saveImageToStorage(
        uri: Uri,
        groupName: String,
        postRandomName: String
    ) {
        val iconPath=groupStorageRef.child("$postRandomName+$groupName")
        iconPath.putFile(uri).addOnSuccessListener {
            Toast.makeText(
                this@GroupInfoActivity,
                "Icon saved successfully to storage",
                Toast.LENGTH_SHORT
            ).show()
            iconPath.downloadUrl.addOnSuccessListener {
                downloadUrl = it.toString()
                saveGroupInfoToDatabase(downloadUrl,groupName)
            }.addOnFailureListener {
                val message= it.message
                Toast.makeText(this@GroupInfoActivity, "Error occured: $message", Toast.LENGTH_SHORT).show()
                loadingBar.dismiss()
            }
        }.addOnFailureListener {
            val message= it.message
            Toast.makeText(this@GroupInfoActivity, "Error occured: $message", Toast.LENGTH_SHORT).show()
            loadingBar.dismiss()
        }
    }

    private fun saveGroupInfoToDatabase(downloadUrl: String, groupName: String) {
        val groupMap = HashMap<String,String>()
        groupMap["name"] = groupName
        groupMap["firstName"]=groupName
        groupMap["image"] = downloadUrl
        groupMap["date"] = saveCurrentDate
        groupMap["time"] = saveCurrentTime
        groupMap["admin"] = currentUserId
        groupMap["recentMessage"] = "Empty conversation"
        groupMap["timeStamp"]=postRandomName
        groupMap["randomName"]=postRandomName
        for (i in this.participantsList!!) {
            userGroupsRef.child(i).child(postRandomName+groupName).setValue(true)
            usersRef.child(postRandomName+groupName).child(i).setValue(true)
        }
        groupsRef.child(postRandomName+groupName).updateChildren(groupMap as Map<String, Any>).addOnCompleteListener {
            if (it.isSuccessful) {
                loadingBar.dismiss()
                Toast.makeText(this@GroupInfoActivity,"Group Created",Toast.LENGTH_SHORT).show()
                sendUserToMessageActivity()
            } else {
                val error=it.exception.toString()
                Toast.makeText(this@GroupInfoActivity,"Error occurred: $error",Toast.LENGTH_LONG).show()
                loadingBar.dismiss()
            }
        }
    }

    private fun sendUserToMessageActivity() {
        val messageIntent=Intent(this@GroupInfoActivity,MessageActivity::class.java)
        messageIntent.putExtra("from","GroupInfoActivity")
        messageIntent.putExtra("postKey","none")
        messageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        messageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(messageIntent)
        finish()
    }
}
