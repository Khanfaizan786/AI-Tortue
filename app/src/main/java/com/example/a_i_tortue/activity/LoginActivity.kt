package com.example.a_i_tortue.activity

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.*
import com.example.a_i_tortue.R

import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.SignInAccount
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.*
import com.google.firebase.database.*
import kotlin.collections.HashMap

class LoginActivity : AppCompatActivity() {

    lateinit var loginFrame:FrameLayout
    lateinit var mAuth: FirebaseAuth
    lateinit var authStateListener: FirebaseAuth.AuthStateListener
    lateinit var usersRef:DatabaseReference

    lateinit var btnLogin: Button
    lateinit var txtCreateAccount: TextView
    lateinit var etEmail: EditText
    lateinit var etPassword: EditText
    var emailAddressChecker:Boolean=false
    lateinit var btnGoogle:Button

    private val TAG="LoginActivity"

    lateinit var loadingBar: ProgressDialog
    lateinit var imgFacebookLogo: ImageView
    val TAGA = "FacebookAuthentication"

    private val RC_SIGN_IN=1
    lateinit var googleSignInClient: GoogleApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth= FirebaseAuth.getInstance()
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users")
        loginFrame=findViewById(R.id.loginFrame)

        //FacebookSdk.sdkInitialize(applicationContext)



        btnLogin=findViewById(R.id.btnLogin)
        txtCreateAccount=findViewById(R.id.txtCreateAccount)
        etEmail=findViewById(R.id.etPhoneNumber)
        etPassword=findViewById(R.id.etPassword)
        btnGoogle=findViewById(R.id.btnGoogle)

        //mCallbackManager= CallbackManager.Factory.create()

        checkUserStatus()

        /*LoginManager.getInstance().registerCallback(mCallbackManager,
            object : FacebookCallback<LoginResult?> {
                override fun onSuccess(loginResult: LoginResult?) {
                    Log.d(TAGA,"onSuccess $loginResult")
                    if (loginResult != null) {
                        handleFacebookToken(loginResult.accessToken)
                    }
                }

                override fun onCancel() {
                    Log.d(TAGA,"onCancel")
                }

                override fun onError(exception: FacebookException) {
                    Log.d(TAGA,"onError $exception")
                }
            })

        authStateListener=FirebaseAuth.AuthStateListener {
            val user=it.currentUser
            if (user!=null) {
                SendUserToMainActivity()
            }
        }*/

        loadingBar= ProgressDialog(this@LoginActivity)

        btnLogin.setOnClickListener {
            AllowingUserToLogin()
        }

        txtCreateAccount.setOnClickListener {

            val intent=Intent(this@LoginActivity,
                RegisterActivity::class.java)
            startActivity(intent)
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient=GoogleApiClient.Builder(this@LoginActivity)
            .enableAutoManage(this@LoginActivity,GoogleApiClient.OnConnectionFailedListener {
                Toast.makeText(this@LoginActivity,"Connection to google sign in failed..!!",Toast.LENGTH_LONG).show()
            })
            .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
            .build()

        btnGoogle.setOnClickListener {
            signIn()
        }

        //imgFacebookLogo.setOnClickListener(this)
    }

    /*private fun handleFacebookToken(accessToken: AccessToken?) {
        Log.d(TAGA,"handleFacebookToken $accessToken")

        val credential=FacebookAuthProvider.getCredential(accessToken?.token as String)
        mAuth.signInWithCredential(credential).addOnCompleteListener(this@LoginActivity, OnCompleteListener {
            if (it.isSuccessful) {
                Log.d(TAGA,"sign in with credential: successfull")
                val user = mAuth.currentUser
                savingDataToFirebaseDatabase(user)
                SendUserToMainActivity()
            } else {
                Log.d(TAGA,"sign in with credential: failure",it.exception)
                Toast.makeText(this@LoginActivity,"Authentication Failed ${it.exception}",Toast.LENGTH_SHORT).show()
            }
        })
    }*/

    private fun savingDataToFirebaseDatabase(user: FirebaseUser?) {

        val postListener=object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LoginActivity,"Error occured: ${error.message}",Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (user != null) {
                    if (!snapshot.hasChild(user.uid)) {

                        val userName= user.displayName
                        val email=user.email
                        val profileImage=user.photoUrl
                        val userId=user.uid

                        val hashMap=HashMap<String,String>()

                        if (userName != null) {
                            hashMap.put("name",userName)
                        } else {
                            hashMap.put("name","No Name")
                        }
                        if (email != null) {
                            hashMap.put("email",email)
                        } else {
                            hashMap.put("email","No Email")
                        }
                        if (profileImage!=null) {
                            hashMap.put("profileImage", profileImage.toString())
                        }
                        hashMap.put("uid",userId)
                        hashMap.put("phone","No Contact No.")
                        hashMap.put("dob","No DOB")
                        hashMap.put("gender","No Gender")
                        hashMap.put("country","No Country")

                        usersRef.child(userId).updateChildren(hashMap as Map<String, Any>).addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(this@LoginActivity,"Your information have been saved successfully..!!",Toast.LENGTH_SHORT).show()
                                loadingBar.dismiss()
                                SendUserToMainActivity()
                            } else {
                                val message=it.getResult().toString()
                                Toast.makeText(this@LoginActivity,"Error Occured: $message",Toast.LENGTH_SHORT).show()
                                loadingBar.dismiss()
                                SendUserToMainActivity()
                            }
                        }
                    } else {
                        loadingBar.dismiss()
                        SendUserToMainActivity()
                    }
                } else {
                    loadingBar.dismiss()
                    SendUserToMainActivity()
                }
            }
        }
        usersRef.addValueEventListener(postListener)
    }


    private fun checkUserStatus() {
        val currentUser: FirebaseUser? =mAuth.currentUser
        if (currentUser!=null) {
            SendUserToMainActivity()
        }
    }

    private fun SendUserToMainActivity() {
        val intent=Intent(this@LoginActivity,
            MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun signIn() {
        val signInIntent:Intent = Auth.GoogleSignInApi.getSignInIntent(googleSignInClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {

            loadingBar.setTitle("Google Sign In")
            loadingBar.setMessage("Please wait while we are authenticating...!!")
            loadingBar.setCanceledOnTouchOutside(true)
            loadingBar.show()

            val result=Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            if (result != null) {
                if (result.isSuccess) {
                    val account=result.signInAccount
                    if (account != null) {
                        firebaseAuthWithGoogle(account.idToken.toString())
                    }
                } else {
                    Toast.makeText(this@LoginActivity,"Can't get results...",Toast.LENGTH_LONG).show()
                    loadingBar.dismiss()
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {

        Log.d(TAG,"firebaseAuthWithGoogle:$idToken")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this@LoginActivity) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user=mAuth.currentUser
                    savingDataToFirebaseDatabase(user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    val message=task.exception.toString()
                    loadingBar.dismiss()
                    Toast.makeText(this@LoginActivity,"Error: $message",Toast.LENGTH_LONG).show()
                }
            }
    }



    override fun onStop() {
        super.onStop()

        if(googleSignInClient.isConnected) {
            googleSignInClient.stopAutoManage(this@LoginActivity)
            googleSignInClient.disconnect()
        }
    }

    private fun AllowingUserToLogin() {
        val email: String = etEmail.getText().toString()
        val password: String = etPassword.getText().toString()
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this@LoginActivity, "Please enter your email...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this@LoginActivity, "Please enter your password...", Toast.LENGTH_SHORT).show()
        } else {
            loadingBar.setTitle("Logging In")
            loadingBar.setMessage("Please wait, while authenticating")
            loadingBar.show()
            loadingBar.setCanceledOnTouchOutside(true)
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        verifyEmailAddress()

                    } else {
                        val message = task.exception!!.message
                        Toast.makeText(
                            this@LoginActivity,
                            "Error occured: $message",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadingBar.dismiss()
                    }
                }
        }
    }

    private fun verifyEmailAddress() {

        val user:FirebaseUser?=mAuth.currentUser
        if (user != null) {
            emailAddressChecker=user.isEmailVerified
        }
        if (emailAddressChecker) {
            loadingBar.dismiss()
            SendUserToMainActivity()
        } else {
            loadingBar.dismiss()
            val builder= AlertDialog.Builder(this@LoginActivity)
            builder.setTitle("Email not verified..!!")
            builder.setMessage("Please go to your Mail inbox where we had sent an Email verification link. If link had not sent, please click on Resend")
            builder.setPositiveButton("Resend link") { _ , _ ->
                loadingBar.setTitle("Please wait..")
                loadingBar.setMessage("Sending verification link to your given Email")
                loadingBar.setCanceledOnTouchOutside(true)
                loadingBar.show()
                if (user!=null) {
                    user.sendEmailVerification().addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this@LoginActivity,"Please check again your Email for verification link",Toast.LENGTH_LONG).show()
                            loadingBar.dismiss()
                        } else {
                            val error= it.exception?.message.toString()
                            loadingBar.dismiss()
                            Toast.makeText(this@LoginActivity,"Error: $error",Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            builder.setNegativeButton("Cancel") { _,_-> }
            builder.create()
            builder.show()
            mAuth.signOut()
        }
    }

    /*override fun onClick(v: View?) {
        if (v == imgFacebookLogo) {
            LoginManager.getInstance().logInWithReadPermissions(
                this,
                Arrays.asList( "email", "public_profile")
            );
        }
    }*/

}