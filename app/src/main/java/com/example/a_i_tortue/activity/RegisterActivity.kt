package com.example.a_i_tortue.activity

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.a_i_tortue.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RegisterActivity : AppCompatActivity() {

    lateinit var toolbarRegister: Toolbar
    lateinit var etRegisterEmail: EditText
    lateinit var etRegisterNewPassword: EditText
    lateinit var etRegisterconfirmPassword: EditText
    lateinit var btnRegister: Button
    lateinit var mAuth: FirebaseAuth
    lateinit var loadingBar: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth= FirebaseAuth.getInstance()
        etRegisterEmail=findViewById(R.id.etRegisterEmail)
        etRegisterNewPassword=findViewById(R.id.etRegisterNewPassword)
        etRegisterconfirmPassword=findViewById(R.id.etRegisterConfirmPassword)
        btnRegister=findViewById(R.id.btnRegister)
        toolbarRegister=findViewById(R.id.toolbarRegister)

        etRegisterEmail.requestFocus()

        loadingBar = ProgressDialog(this)

        setSupportActionBar(toolbarRegister)
        supportActionBar?.title="Register Yourself"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btnRegister.setOnClickListener {
            CreateNewAccount()
        }
    }

    private fun CreateNewAccount() {
        val email: String = etRegisterEmail.text.toString()
        val password: String = etRegisterNewPassword.text.toString()
        val confirmPassword: String = etRegisterconfirmPassword.text.toString()
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this@RegisterActivity, "Please write your email...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this@RegisterActivity, "Please write your password...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this@RegisterActivity, "Please confirm your password...", Toast.LENGTH_SHORT).show()
        } else if (password != confirmPassword) {
            Toast.makeText(this@RegisterActivity, "Password not match...", Toast.LENGTH_SHORT).show()
        } else {
            loadingBar.setTitle("Creating New Account")
            loadingBar.setMessage("Please wait, while checking credentials")
            loadingBar.show()
            loadingBar.setCanceledOnTouchOutside(true)
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        sendEmailVerificationMessage()

                    } else {
                        val message = task.exception!!.message
                        Toast.makeText(
                            this@RegisterActivity,
                            "Error occured: $message",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadingBar.dismiss()
                    }
                }
        }
    }

    private fun sendEmailVerificationMessage() {
        val user: FirebaseUser?=mAuth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this@RegisterActivity,"Please check your Email for verification link",Toast.LENGTH_LONG).show()
                mAuth.signOut()
                loadingBar.dismiss()
                SendUserToLoginActivity()
            } else {
                val error= it.exception?.message.toString()
                mAuth.signOut()
                loadingBar.dismiss()
                Toast.makeText(this@RegisterActivity,"Error: $error",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun SendUserToLoginActivity() {
        val intent= Intent(this@RegisterActivity,
            LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}