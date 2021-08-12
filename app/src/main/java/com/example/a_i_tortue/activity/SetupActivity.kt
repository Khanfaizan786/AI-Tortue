package com.example.a_i_tortue.activity

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.a_i_tortue.R
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
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class SetupActivity : AppCompatActivity() {

    private val CAMERA_REQUEST_CODE = 100
    private val STORAGE_REQUEST_CODE = 200

    private var saveCurrentDate: String? = null
    private  var saveCurrentTime:String? = null
    private  var postRandomName:String? = null
    private  var downloadUrl:String? = null

    private lateinit var mAuth:FirebaseAuth
    private lateinit var usersRef:DatabaseReference
    private lateinit var storageRef:StorageReference

    lateinit var cameraPermissions: Array<String>
    lateinit var storagePermissions: Array<String>

    lateinit var txtDOB:TextView
    lateinit var spnrCountry:Spinner
    lateinit var spnrGender:Spinner
    lateinit var imgSetupProfile:CircleImageView
    lateinit var btnSetupSubmit:Button
    lateinit var etSetupName:EditText
    lateinit var etSetupContact:EditText
    lateinit var loadingBar: ProgressDialog

    lateinit var toolbarSetup: Toolbar

    private var resultUri: Uri? = null
    private lateinit var date:String

    private lateinit var adapterGender:ArrayAdapter<String>
    private lateinit var adapterCountry:ArrayAdapter<String>

    lateinit var setListener:DatePickerDialog.OnDateSetListener
    private lateinit var currentUserId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        txtDOB=findViewById(R.id.txtDOB)
        spnrCountry=findViewById(R.id.spnrCountry)
        spnrGender=findViewById(R.id.spnrGender)
        imgSetupProfile=findViewById(R.id.imgSetupProfile)
        btnSetupSubmit=findViewById(R.id.btnSetupSubmit)
        etSetupName=findViewById(R.id.etSetupName)
        etSetupContact=findViewById(R.id.etSetupContact)

        toolbarSetup=findViewById(R.id.toolbarSetup)

        loadingBar= ProgressDialog(this@SetupActivity)

        setSupportActionBar(toolbarSetup)
        supportActionBar?.title="Setup Profile"

        mAuth=FirebaseAuth.getInstance()
        currentUserId= mAuth.currentUser?.uid.toString()
        usersRef =FirebaseDatabase.getInstance().reference.child("Users").child(currentUserId)
        storageRef=FirebaseStorage.getInstance().reference.child("Profile Images")

        cameraPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        storagePermissions =
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

        val calendar=Calendar.getInstance()
        val year=calendar.get(Calendar.YEAR)
        val month=calendar.get(Calendar.MONTH)
        val day=calendar.get(Calendar.DAY_OF_MONTH)

        txtDOB.setOnClickListener {
            val datePickerDialog=DatePickerDialog(
                this@SetupActivity,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                setListener,year,month,day)
            datePickerDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            datePickerDialog.show()
        }

        setListener= DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            date="$dayOfMonth/${month+1}/$year"
            txtDOB.text=date
        }

        populateSpinnerCountry()
        populateSpinnerGender()

        val postListener=object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                val message=error.message
                Toast.makeText(this@SetupActivity,"Error occured: $message",Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild("profileImage")) {
                    val myProfileImage=snapshot.child("profileImage").value.toString()
                    Picasso.with(this@SetupActivity).load(myProfileImage).placeholder(
                        R.drawable.profile
                    ).into(imgSetupProfile)
                }
                if (snapshot.hasChild("name")) {

                    supportActionBar?.title="Update Profile"
                    btnSetupSubmit.text="Update"

                    val myFullName:String = snapshot.child("name").value.toString()
                    val myCountry: String = snapshot.child("country").value.toString()
                    val myContact: String = snapshot.child("phone").value.toString()
                    val myDOB: String = snapshot.child("dob").value.toString()
                    val myGender: String = snapshot.child("gender").value.toString()

                    val posGender=adapterGender.getPosition(myGender)
                    spnrGender.setSelection(posGender)

                    val posCountry=adapterCountry.getPosition(myCountry)
                    spnrCountry.setSelection(posCountry)

                    if (myFullName != "No Name") {
                        etSetupName.setText(myFullName)
                    }
                    if (myContact != "No Contact No.") {
                        etSetupContact.setText(myContact)
                    }
                    if (myDOB != "No DOB") {
                        txtDOB.text = myDOB
                    }
                }
            }
        }

        usersRef.addValueEventListener(postListener)

        imgSetupProfile.setOnClickListener {
            pickFromGallery()
        }

        btnSetupSubmit.setOnClickListener {
            validateUserInformation()
        }
    }

    private fun validateUserInformation() {
        val name=etSetupName.text.toString()
        var phone=etSetupContact.text.toString()
        var dob=txtDOB.text.toString()
        val gender=spnrGender.selectedItem.toString()
        val country=spnrCountry.selectedItem.toString()
        val email= mAuth.currentUser?.email

        if (TextUtils.isEmpty(phone)) {
            phone="No Contact No."
        }
        if (TextUtils.isEmpty(dob)) {
            dob="No DOB"
        }
        if (TextUtils.isEmpty(name)) {
            val builder = AlertDialog.Builder(this@SetupActivity)
            builder.setTitle("Name is mandatory..!!")
            builder.setMessage("Please provide your name")
            builder.setPositiveButton("Ok") { _,_->}
            builder.create().show()
        } else {
            loadingBar.setTitle("Saving Information")
            loadingBar.setMessage("Please wait while we are saving your information...!!")
            loadingBar.setCanceledOnTouchOutside(true)
            loadingBar.show()
            if (resultUri!=null) {
                val file: File = File(
                    SiliCompressor.with(this)
                        .compress(
                            FileUtils.getPath(this,resultUri), File(this.cacheDir,"temp")
                        ))
                val uri=Uri.fromFile(file)
                savingImageToFirebaseStorage(uri.toString(),name,phone,dob,gender,country,email)
            }
            else {
                savingInformationToDatabase(name,phone,dob,gender,country,email)
            }
        }
    }

    private fun savingInformationToDatabase(
        name: String,
        phone: String,
        dob: String,
        gender: String,
        country: String,
        email: String?
    ) {
        val profileMap=HashMap<String,String>()
        profileMap["name"] = name
        profileMap["phone"] = phone
        profileMap["dob"] = dob
        profileMap["gender"] = gender
        profileMap["country"] = country
        profileMap["email"] = email.toString()
        profileMap["uid"] = currentUserId

        usersRef.updateChildren(profileMap as Map<String, Any>).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this@SetupActivity, "Information saved successfully..!!", Toast.LENGTH_SHORT).show()
                loadingBar.dismiss()
                sendUserToMainActivity()
            }
            else {
                val message= it.exception?.message
                Toast.makeText(this@SetupActivity, "Error occured: $message", Toast.LENGTH_SHORT).show()
                loadingBar.dismiss()
            }
        }
    }

    private fun sendUserToMainActivity() {
        val intent=Intent(this@SetupActivity,
            MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun savingImageToFirebaseStorage(
        resultUri: String,
        name: String,
        phone: String,
        dob: String,
        gender: String,
        country: String,
        email: String?
    ) {
        val filePath:StorageReference=storageRef.child("$currentUserId.jpg")
        filePath.putFile(Uri.parse(resultUri)).addOnSuccessListener {
            Toast.makeText(this@SetupActivity, "Image uploaded successfully to storage", Toast.LENGTH_SHORT).show()
            filePath.downloadUrl.addOnSuccessListener {
                val downloadUrl=it.toString()
                savingUserInformationToDatabase(name,phone,dob,gender,country,downloadUrl,email)
            }
        }.addOnFailureListener {
            val message= it.message
            Toast.makeText(this@SetupActivity, "Error occured: $message", Toast.LENGTH_SHORT).show()
            loadingBar.dismiss()
        }
    }

    private fun savingUserInformationToDatabase(
        name: String,
        phone: String,
        dob: String,
        gender: String,
        country: String,
        downloadUrl: String,
        email: String?
    ) {
        val profileMap=HashMap<String,String>()
        profileMap.put("name",name)
        profileMap.put("phone",phone)
        profileMap.put("dob",dob)
        profileMap.put("gender",gender)
        profileMap.put("country",country)
        profileMap.put("profileImage",downloadUrl)
        profileMap.put("email",email.toString())
        profileMap.put("uid",currentUserId)

        usersRef.updateChildren(profileMap as Map<String, Any>).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this@SetupActivity, "Information saved successfully..!!", Toast.LENGTH_SHORT).show()
                loadingBar.dismiss()
                sendUserToMainActivity()
            }
            else {
                val message= it.exception?.message
                Toast.makeText(this@SetupActivity, "Error occured: $message", Toast.LENGTH_SHORT).show()
                loadingBar.dismiss()
            }
        }
    }

    private fun pickFromGallery() {
        if (!checkCameraPermission()) {
            requestCameraPermission()
            if (!checkStoragePermission()) {
                requestStoragePermission()
            }
        } else if (!checkStoragePermission()) {
            requestStoragePermission()
        }
        else {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this@SetupActivity)
        }
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            storagePermissions,
            STORAGE_REQUEST_CODE
        )
    }

    private fun checkCameraPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val result1 = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        return result && result1
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            cameraPermissions,
            CAMERA_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                resultUri = result.uri
                Picasso.with(this@SetupActivity).load(resultUri).placeholder(R.drawable.profile)
                    .into(imgSetupProfile)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val exception = result.error
                Toast.makeText(this, "Error occured: $exception", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateSpinnerGender() {
        adapterGender=ArrayAdapter<String>(this@SetupActivity,android.R.layout.simple_spinner_item,resources.getStringArray(
            R.array.Genders
        ))
        adapterGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnrGender.adapter=adapterGender
    }

    private fun populateSpinnerCountry() {
        val locale=Locale.getAvailableLocales()
        val countries= ArrayList<String>()
        var country:String
        for (loc in locale) {
            country = loc.displayCountry
            if (country.isNotEmpty() && !countries.contains(country)) {
                countries.add(country)
            }
        }
        Collections.sort(countries,String.CASE_INSENSITIVE_ORDER)
        countries.add(0,"Select")

        adapterCountry=ArrayAdapter<String>(this@SetupActivity,android.R.layout.simple_spinner_item,countries)
        adapterCountry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnrCountry.adapter=adapterCountry
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
                            this,
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
                            this,
                            "Storage permissions are necessary",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}