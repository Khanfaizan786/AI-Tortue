package com.example.a_i_tortue.activity

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.example.a_i_tortue.fragment.ProfileFragment
import com.example.a_i_tortue.R
import com.example.a_i_tortue.fragment.HomeFragment
import com.example.a_i_tortue.fragment.PostFragment
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavView:BottomNavigationView
    private lateinit var frameLayout: FrameLayout
    private lateinit var usersRef:DatabaseReference
    private lateinit var mAuth:FirebaseAuth
    val TAG="MainActivity"
    private lateinit var currentUserId:String
    private var from:String?="Nothing"

    lateinit var googleSignInClient: GoogleApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth= FirebaseAuth.getInstance()
        currentUserId= mAuth.currentUser?.uid.toString()
        usersRef=FirebaseDatabase.getInstance().reference.child("Users").child(currentUserId)

        bottomNavView=findViewById(R.id.bottomNavView)
        frameLayout=findViewById(R.id.frame)

        if (intent!=null) {
            from=intent.getStringExtra("from")
            val userId=intent.getStringExtra("userId")
            val postKey=intent.getStringExtra("postKey")

            if (from=="SingleChatActivity") {
                val bundle=Bundle()
                bundle.putString("from",from)
                bundle.putString("userId",userId)
                val profileFragment=ProfileFragment()
                profileFragment.arguments=bundle

                val transaction=supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame,profileFragment)
                transaction.commit()

                bottomNavView.selectedItemId=R.id.profile
                bottomNavView.visibility=View.GONE

            } else if (from=="ChatActivity"){
                val bundle=Bundle()
                bundle.putString("postKey",postKey)
                val postFragment=PostFragment()
                postFragment.arguments=bundle
                val transaction=supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame,postFragment)
                transaction.commit()
                bottomNavView.selectedItemId=R.id.homeBlog
                bottomNavView.visibility=View.GONE
            } else {
                openHomePage()
            }

        } else {
            openHomePage()
        }

        checkUserExistence()

        bottomNavView.setOnNavigationItemSelectedListener {

            when (it.itemId) {
                R.id.homeBlog -> {
                    if (from != "SingleChatActivity") {
                        val fragment= PostFragment()
                        val transaction=supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.frame,fragment)
                        transaction.commit()
                    } else {
                        bottomNavView.selectedItemId=R.id.profile
                    }
                }

                R.id.profile -> {
                    if (from != "SingleChatActivity") {
                        val fragment= ProfileFragment()
                        val transaction=supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.frame,fragment)
                        transaction.commit()
                    } else {
                        bottomNavView.selectedItemId=R.id.profile
                    }
                }

                R.id.dashboard -> {
                    if (from != "SingleChatActivity") {
                        val fragment= HomeFragment()
                        val transaction=supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.frame,fragment)
                        transaction.commit()
                    } else {
                        bottomNavView.selectedItemId=R.id.profile
                    }
                }

                R.id.chat -> {

                    /*val dialog=AlertDialog.Builder(this)
                    dialog.setTitle("Log Out?")
                    dialog.setMessage("Are you sure want to log out?")
                    dialog.setPositiveButton("Yes") { _,_ ->
                        logOut()
                    }
                    dialog.setNegativeButton("Cancel") { _,_-> }
                    dialog.create().show()*/

                    if (from != "SingleChatActivity") {
                        val intent=Intent(this@MainActivity,MessageActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        bottomNavView.selectedItemId=R.id.profile
                    }
                }
                else -> Toast.makeText(this@MainActivity,"Something went wrong",Toast.LENGTH_SHORT).show()
            }
            return@setOnNavigationItemSelectedListener true
        }
    }

    private fun logOut() {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient= GoogleApiClient.Builder(this@MainActivity)
            .enableAutoManage(this@MainActivity, GoogleApiClient.OnConnectionFailedListener {
                Toast.makeText(this@MainActivity,"Connection to google sign in failed..!!", Toast.LENGTH_LONG).show()
            })
            .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
            .build()

        val callBacks=object: GoogleApiClient.ConnectionCallbacks{
            override fun onConnected(p0: Bundle?) {
                if (googleSignInClient.isConnected) {
                    Auth.GoogleSignInApi.signOut(googleSignInClient).setResultCallback {
                        if (it.isSuccess) {
                            mAuth.signOut()
                            Log.d(TAG,"User Logged out")
                            sendUserToLoginActivity()
                        }
                    }
                } else {
                    Toast.makeText(this@MainActivity,"Something went wrong",Toast.LENGTH_LONG).show()
                }
            }

            override fun onConnectionSuspended(p0: Int) {

            }
        }
        googleSignInClient.registerConnectionCallbacks(callBacks)
    }

    private fun sendUserToLoginActivity() {
        val intent=Intent(this@MainActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun openHomePage() {
        val fragment= PostFragment()
        val transaction=supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame,fragment)
        transaction.commit()
        bottomNavView.selectedItemId=R.id.homeBlog
    }

    private fun checkUserExistence() {
        val postListener=object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                val message=error.message
                Toast.makeText(this@MainActivity,"Error occured: $message",Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.hasChild("name")) {
                    sendUserToSetupActivity()
                }
            }

        }
        usersRef.addValueEventListener(postListener)
    }

    override fun onBackPressed() {

        val fragment=supportFragmentManager.findFragmentById(R.id.frame)
        if (fragment is PostFragment)
        {
            super.onBackPressed()
        }
        else if (fragment is ProfileFragment && from=="SingleChatActivity"){
            super.onBackPressed()
        } else {
            openHomePage()
        }
    }

    private fun sendUserToSetupActivity() {
        val intent=Intent(this@MainActivity, SetupActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
