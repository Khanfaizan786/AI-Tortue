package com.example.a_i_tortue.fragment

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.example.a_i_tortue.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.iceteck.silicompressorr.FileUtils
import com.iceteck.silicompressorr.SiliCompressor
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File

/**
 * A simple [Fragment] subclass.
 */
class DevelopersFragment : Fragment() {

    private val CAMERA_REQUEST_CODE = 100
    private val STORAGE_REQUEST_CODE = 200

    lateinit var storagePermissions: Array<String>
    lateinit var cameraPermissions: Array<String>

    lateinit var storageRef:StorageReference
    lateinit var storageRefImg:StorageReference
    lateinit var storageRefPdf:StorageReference
    lateinit var databaseRef:DatabaseReference
    lateinit var databaseRefDoc:DatabaseReference

    private lateinit var btnPickFile:Button
    private lateinit var btnPickDocument:Button
    private lateinit var txtFilepath:TextView
    private lateinit var txtDocumentpath:TextView
    private lateinit var btnUploadFile:Button
    private lateinit var btnUploadDocument:Button
    private lateinit var etUploadFileName:EditText
    private lateinit var etDocumentName:EditText
    private lateinit var imgUploadFile:ImageView
    private val PICK_FILE=1
    private val PICK_IMAGE=2
    private val PICK_DOCUMENT=3
    private var resultUri: Uri? = null
    private var fileUri: Uri? = null
    private var documentUri: Uri? = null
    private var dataUrl:String?=null
    private var dataImgUrl:String?=null
    lateinit var loadingBar: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view=inflater.inflate(R.layout.fragment_developers, container, false)

        storageRef=FirebaseStorage.getInstance().reference.child("Datasets")
        storageRefImg=FirebaseStorage.getInstance().reference.child("Dataset Images")
        storageRefPdf=FirebaseStorage.getInstance().reference.child("Documents")
        databaseRef=FirebaseDatabase.getInstance().reference.child("Data")
        databaseRefDoc=FirebaseDatabase.getInstance().reference.child("Documents")

        btnPickFile=view.findViewById(R.id.btnPickFile)
        txtFilepath=view.findViewById(R.id.txtFilepath)
        btnUploadFile=view.findViewById(R.id.btnUploadFile)
        etUploadFileName=view.findViewById(R.id.etUploadFileName)
        imgUploadFile=view.findViewById(R.id.imgUploadFile)
        btnPickDocument=view.findViewById(R.id.btnPickDocument)
        txtDocumentpath=view.findViewById(R.id.txtDocumentPath)
        btnUploadDocument=view.findViewById(R.id.btnUploadDocument)
        etDocumentName=view.findViewById(R.id.etDocumentName)

        loadingBar= ProgressDialog(activity)

        storagePermissions =
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

        cameraPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        btnPickFile.setOnClickListener {
            checkPermissions()
        }

        imgUploadFile.setOnClickListener {
            pickFromGallery()
        }

        btnUploadFile.setOnClickListener {
            uploadFile()
        }

        btnPickDocument.setOnClickListener {
            checkStoragePermissions()
        }

        btnUploadDocument.setOnClickListener {
            savingPdfToStorage()
        }

        return view
    }

    private fun savingPdfToStorage() {
        val pdfName=etDocumentName.text.toString()
        if (documentUri==null) {
            Toast.makeText(activity,"Please select a PDF file",Toast.LENGTH_LONG).show()
        } else if (TextUtils.isEmpty(pdfName)) {
            Toast.makeText(activity,"Please give a name to file",Toast.LENGTH_LONG).show()
        } else {
            loadingBar.setTitle("Saving File")
            loadingBar.setMessage("Please wait while we are saving file...!!")
            loadingBar.setCanceledOnTouchOutside(true)
            loadingBar.show()

            val filePathPdf:StorageReference=storageRefPdf.child("${documentUri!!.lastPathSegment} ${System.currentTimeMillis()}")
            filePathPdf.putFile(documentUri!!).addOnSuccessListener {
                Toast.makeText(activity,"Document file is saved to storage successfully",Toast.LENGTH_LONG).show()
                filePathPdf.downloadUrl.addOnSuccessListener {
                    val pdfUri=it.toString()
                    saveDocumentDetailsToDatabase(pdfName,pdfUri)
                }.addOnFailureListener {
                    val message= it.message
                    Toast.makeText(activity, "Error occured: $message", Toast.LENGTH_SHORT).show()
                    loadingBar.dismiss()
                }
            }.addOnFailureListener {
                val message= it.message
                Toast.makeText(activity, "Error occured: $message", Toast.LENGTH_SHORT).show()
                loadingBar.dismiss()
            }
        }
    }

    private fun saveDocumentDetailsToDatabase(pdfName: String, pdfUri: String) {
        val documentMap=HashMap<String,String>()
        documentMap.put("name",pdfName)
        documentMap.put("document",pdfUri)

        databaseRefDoc.child("${System.currentTimeMillis()}$pdfName").updateChildren(documentMap as Map<String, Any>).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(activity, "Document saved successfully to database..!!", Toast.LENGTH_SHORT).show()
                loadingBar.dismiss()
            } else {
                val message= it.exception?.message
                Toast.makeText(activity, "Error occured: $message", Toast.LENGTH_SHORT).show()
                loadingBar.dismiss()
            }
        }
    }

    private fun checkStoragePermissions() {
        if (!checkStoragePermission()) {
            requestStoragePermission()
        } else {
            pickDocument()
        }
    }

    private fun pickDocument() {
        val intent=Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startActivityForResult(intent,PICK_DOCUMENT)
    }

    private fun uploadFile() {

        val fileName=etUploadFileName.text.toString()
        if (TextUtils.isEmpty(fileName)) {
            Toast.makeText(activity,"Please provide file name",Toast.LENGTH_LONG).show()
        } else if (fileUri==null){
            Toast.makeText(activity,"Please choose a data file",Toast.LENGTH_LONG).show()
        } else if (resultUri==null) {
            Toast.makeText(activity,"Please select an image for data",Toast.LENGTH_LONG).show()
        } else {

            loadingBar.setTitle("Saving Data")
            loadingBar.setMessage("Please wait while we are saving data...!!")
            loadingBar.setCanceledOnTouchOutside(true)
            loadingBar.show()

            val file: File = File(
                SiliCompressor.with(activity)
                    .compress(
                        FileUtils.getPath(activity,resultUri), File(activity?.cacheDir,"temp")
                    ))
            val uri=Uri.fromFile(file)

            val filePathImg:StorageReference=storageRefImg.child("${resultUri!!.lastPathSegment} ${System.currentTimeMillis()}")
            val filePath:StorageReference=storageRef.child("${fileUri!!.lastPathSegment} ${System.currentTimeMillis()}")

            filePath.putFile(fileUri!!).addOnSuccessListener {
                Toast.makeText(activity,"Data file is saved to storage successfully",Toast.LENGTH_LONG).show()
                filePath.downloadUrl.addOnSuccessListener {
                    dataUrl=it.toString()
                    filePathImg.putFile(uri).addOnSuccessListener {
                        Toast.makeText(activity,"Data image is saved to storage successfully",Toast.LENGTH_LONG).show()
                        filePathImg.downloadUrl.addOnSuccessListener {
                            dataImgUrl=it.toString()
                            saveDataToDatabase(dataUrl!!,dataImgUrl!!,fileName)
                        }.addOnFailureListener {
                            val message= it.message
                            Toast.makeText(activity, "Error occured: $message", Toast.LENGTH_SHORT).show()
                            loadingBar.dismiss()
                        }
                    }.addOnFailureListener {
                        val message= it.message
                        Toast.makeText(activity, "Error occured: $message", Toast.LENGTH_SHORT).show()
                        loadingBar.dismiss()
                    }
                }.addOnFailureListener {
                    val message= it.message
                    Toast.makeText(activity, "Error occured: $message", Toast.LENGTH_SHORT).show()
                    loadingBar.dismiss()
                }
            }.addOnFailureListener {
                val message= it.message
                Toast.makeText(activity, "Error occured: $message", Toast.LENGTH_SHORT).show()
                loadingBar.dismiss()
            }
        }
    }

    private fun saveDataToDatabase(
        dataUrl: String,
        dataImgUrl: String,
        fileName: String
    ) {
        val dataMap=HashMap<String,String>()
        dataMap["file"] = dataUrl
        dataMap["image"] = dataImgUrl
        dataMap["name"] = fileName

        databaseRef.child("${System.currentTimeMillis()}$fileName").updateChildren(dataMap as Map<String, Any>).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(activity, "Data saved successfully..!!", Toast.LENGTH_SHORT).show()
                loadingBar.dismiss()
            }
            else {
                val message= it.exception?.message
                Toast.makeText(activity, "Error occured: $message", Toast.LENGTH_SHORT).show()
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
            val intent=Intent(Intent.ACTION_GET_CONTENT)
            intent.setType("image/*")
            startActivityForResult(Intent.createChooser(intent,"Select Image"),PICK_IMAGE)
        }
    }


    private fun checkCameraPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            activity as Context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val result1 = ContextCompat.checkSelfPermission(
            activity as Context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        return result && result1
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            activity as Activity,
            cameraPermissions,
            CAMERA_REQUEST_CODE
        )
    }

    private fun filePick() {
        val intent=Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/zip"
        startActivityForResult(intent,PICK_FILE)
    }

    private fun checkPermissions() {
        if (!checkStoragePermission()) {
            requestStoragePermission()
        } else {
            filePick()
        }
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity as Context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            activity as Activity,
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
                    if (!(cameraAccepted && storageAccepted))  {
                        Toast.makeText(
                            activity,
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
                    if (!writeStorageAccepted){
                        Toast.makeText(
                            activity,
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

        if (requestCode==PICK_FILE) {
            if (resultCode==Activity.RESULT_OK) {
                fileUri= data?.data
                if (fileUri != null) {
                    txtFilepath.text= fileUri.toString()
                }
            }
        }

        if (requestCode == PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    resultUri = data.data
                }
                Picasso.with(activity).load(resultUri).placeholder(R.drawable.profile)
                    .into(imgUploadFile)
            }
        }

        if (requestCode == PICK_DOCUMENT) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    documentUri = data.data
                }
                txtDocumentpath.text=documentUri.toString()
            }
        }
    }
}