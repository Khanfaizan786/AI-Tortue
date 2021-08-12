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
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.a_i_tortue.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.iceteck.silicompressorr.SiliCompressor
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddPostActivity : AppCompatActivity() {

    private val GALLERY_REQUEST_CODE = 300
    private val CAPTURE_REQUEST_CODE = 400
    private val CAMERA_REQUEST_CODE = 100
    private val STORAGE_REQUEST_CODE = 200

    lateinit var cameraPermissions: Array<String>
    lateinit var storagePermissions: Array<String>
    private lateinit var loadingBar:ProgressDialog

    private var resultUri: Uri? = null

    private lateinit var saveCurrentDate: String
    private lateinit var saveCurrentTime: String
    private lateinit var postRandomName: String
    private var downloadUrl: String="none"
    private lateinit var currentUserId: String
    private lateinit var mAuth: FirebaseAuth

    private lateinit var postRef: DatabaseReference
    private lateinit var usersRef: DatabaseReference
    private lateinit var postStorageRef: StorageReference

    private lateinit var etPostTitle: EditText
    private lateinit var etPostDescription: EditText
    private lateinit var imgSelectPost: ImageView
    private lateinit var btnUpdatePost: FloatingActionButton
    private lateinit var crdViewAddPost:CardView
    private lateinit var toolbarAddPost:Toolbar
    private lateinit var staticTextAddImage:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        etPostTitle = findViewById(R.id.etPostTitle)
        etPostDescription = findViewById(R.id.etPostDescription)
        imgSelectPost = findViewById(R.id.imgSelectPost)
        btnUpdatePost = findViewById(R.id.btnUpdatePost)
        crdViewAddPost = findViewById(R.id.crdViewAddPost)
        toolbarAddPost = findViewById(R.id.addPostToolbar)
        staticTextAddImage = findViewById(R.id.staticTxtAddImage)

        setSupportActionBar(toolbarAddPost)
        supportActionBar?.title = "Add Post"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth.currentUser?.uid.toString()
        postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        usersRef = FirebaseDatabase.getInstance().reference.child("Users")
        postStorageRef = FirebaseStorage.getInstance().reference.child("Post Images")

        loadingBar= ProgressDialog(this@AddPostActivity)

        cameraPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        storagePermissions =
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

        crdViewAddPost.setOnClickListener {
            pickFromGallery()
        }

        imgSelectPost.setOnClickListener {
            pickFromGallery()
        }

        btnUpdatePost.setOnClickListener {
            validatePostInfo()
        }
    }

    private fun validatePostInfo() {
        var title = etPostTitle.text.toString()
        var description = etPostDescription.text.toString()

        if (TextUtils.isEmpty(description) && resultUri==null) {
            Toast.makeText(this@AddPostActivity, "Please select an image or add a caption", Toast.LENGTH_SHORT).show()
        }  else {
            if (TextUtils.isEmpty(title)) {
                title = "none"
            }
            if (TextUtils.isEmpty(description)) {
                description = "none"
            }
            if (resultUri == null) {
                val callForDate = Calendar.getInstance()
                val currentDate2 = SimpleDateFormat("dd MMM yyyy")
                val chatDate = currentDate2.format(callForDate.time)

                val callForTime = Calendar.getInstance()
                val currentTime2 = SimpleDateFormat("hh:mm aa")
                val chatTime = currentTime2.format(callForTime.time)

                val currentDate = SimpleDateFormat("yyyy-MM-dd")
                saveCurrentDate = currentDate.format(callForDate.time)

                val currentTime = SimpleDateFormat("HH:mm:ss")
                saveCurrentTime = currentTime.format(callForTime.time)

                postRandomName = saveCurrentDate + saveCurrentTime

                savePostInfoToDatabase(title,description,chatDate,chatTime)
            } else {
                loadingBar.setTitle("Uploading Post")
                loadingBar.setMessage("Please wait while we are uploading...!!")
                loadingBar.setCanceledOnTouchOutside(true)
                loadingBar.show()
                savePostImageToStorage(title, description, resultUri!!)
            }
        }
    }

    private fun savePostImageToStorage(
        title: String,
        description: String,
        uri: Uri
    ) {
        val callForDate = Calendar.getInstance()
        val currentDate = SimpleDateFormat("yyyy-MM-dd")
        saveCurrentDate = currentDate.format(callForDate.time)

        val callForTime = Calendar.getInstance()
        val currentTime = SimpleDateFormat("HH:mm:ss")
        saveCurrentTime = currentTime.format(callForTime.time)

        val currentDate2=SimpleDateFormat("dd MMM yyyy")
        val chatDate=currentDate2.format(callForDate.time)

        val currentTime2 = SimpleDateFormat("hh:mm aa")
        val chatTime = currentTime2.format(callForTime.time)

        postRandomName = saveCurrentDate + saveCurrentTime

        val filePath = postStorageRef.child("${resultUri?.lastPathSegment}$postRandomName")
        filePath.putFile(uri).addOnSuccessListener {
            Toast.makeText(
                this@AddPostActivity,
                "Image uploaded successfully to storage",
                Toast.LENGTH_SHORT
            ).show()
            filePath.downloadUrl.addOnSuccessListener {
                downloadUrl = it.toString()
                savePostInfoToDatabase(title, description,chatDate,chatTime)
            }.addOnFailureListener {
                val message= it.message
                Toast.makeText(this@AddPostActivity, "Error occured: $message", Toast.LENGTH_SHORT).show()
                loadingBar.dismiss()
            }
        }.addOnFailureListener {
            val message= it.message
            Toast.makeText(this@AddPostActivity, "Error occured: $message", Toast.LENGTH_SHORT).show()
            loadingBar.dismiss()
        }
    }

    private fun savePostInfoToDatabase(
        title: String,
        description: String,
        chatDate: String,
        chatTime: String
    ) {
        usersRef.child(currentUserId).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                val message = error.message
                Toast.makeText(this@AddPostActivity, "Error occured: $message", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) run {
                    val fullName = snapshot.child("name").value.toString()
                    var profileImage: String? = null
                    if (snapshot.hasChild("profileImage")) {
                        profileImage = snapshot.child("profileImage").value.toString()
                    }
                    val postsMap = HashMap<String, String>()
                    postsMap["uid"] = currentUserId
                    postsMap["date"] = chatDate
                    postsMap["time"] = chatTime
                    postsMap["description"] = description
                    postsMap["title"] = title
                    postsMap["postImage"] = downloadUrl
                    postsMap["fullName"] = fullName
                    if (profileImage != null) {
                        postsMap["profileImage"] = profileImage
                    }
                    postsMap["randomName"] = postRandomName

                    postRef.child(postRandomName + currentUserId)
                        .updateChildren(postsMap as Map<String, Any>).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this@AddPostActivity, "Information saved successfully..!!", Toast.LENGTH_SHORT).show()
                            loadingBar.dismiss()
                            sendUserToMainActivity()
                        } else {
                            val message = it.exception?.message
                            Toast.makeText(
                                this@AddPostActivity,
                                "Error occured: $message",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadingBar.dismiss()
                        }
                    }
                }
            }
        })
    }

    private fun sendUserToMainActivity() {
        val intent=Intent(this@AddPostActivity,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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
            CropImage.activity().setInitialCropWindowPaddingRatio(0F).start(this@AddPostActivity)
        }
    }

    private fun checkCameraPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this@AddPostActivity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val result1 = ContextCompat.checkSelfPermission(
            this@AddPostActivity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        return result && result1
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this@AddPostActivity,
            cameraPermissions,
            CAMERA_REQUEST_CODE
        )
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this@AddPostActivity,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this@AddPostActivity,
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
                            this@AddPostActivity,
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
                            this@AddPostActivity,
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
                    compressImage(resultUri!!)
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val exception = result.error
                Toast.makeText(this, "Error occured: $exception", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun compressImage(resultUri: Uri) {

        staticTextAddImage.visibility= View.GONE

        val file:File=File(SiliCompressor.with(this)
            .compress(com.iceteck.silicompressorr
                .FileUtils.getPath(this,resultUri), File(this.cacheDir,"temp")
            ))
        this.resultUri=Uri.fromFile(file)
        Picasso.with(this@AddPostActivity).load(this.resultUri)
            .placeholder(R.drawable.select_image)
            .into(imgSelectPost)
    }
}