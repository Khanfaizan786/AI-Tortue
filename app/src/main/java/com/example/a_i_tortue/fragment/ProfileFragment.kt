package com.example.a_i_tortue.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.a_i_tortue.R
import com.example.a_i_tortue.activity.LoginActivity
import com.example.a_i_tortue.activity.SetupActivity
import com.example.a_i_tortue.adapter.ProfilePostsAdapter
import com.example.a_i_tortue.model.Posts
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.*

class ProfileFragment : Fragment() {

    private lateinit var mAuth:FirebaseAuth
    private lateinit var usersRef:DatabaseReference
    private lateinit var currentUserId:String
    private lateinit var currentUserIdDuplicate:String
    private lateinit var recyclerProfilePosts: RecyclerView
    private lateinit var layoutManager: GridLayoutManager
    private var profilePostList= arrayListOf<Posts>()
    private lateinit var profilePostsAdapter: ProfilePostsAdapter
    private lateinit var postsQuery: Query

    private lateinit var imgPersonProfilePic:CircleImageView
    private lateinit var profileToolbar:Toolbar
    private lateinit var txtPersonProfileName:TextView
    private lateinit var txtPersonProfileCountry:TextView
    private lateinit var txtPersonProfileEmail:TextView
    private lateinit var txtPersonProfileContact:TextView
    private lateinit var txtPersonProfileDob:TextView
    private lateinit var txtPersonProfileGender:TextView
    private lateinit var imgBtnLogOut:ImageButton
    private lateinit var imgBtnEditProfile:ImageButton
    private lateinit var myProfileImage:String

    lateinit var googleSignInClient: GoogleApiClient
    val TAG="MainActivity"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view= inflater.inflate(R.layout.fragment_profile, container, false)

        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth.currentUser?.uid.toString()
        currentUserIdDuplicate= mAuth.currentUser?.uid.toString()

        setHasOptionsMenu(true)

        if (arguments!=null) {
            currentUserId = arguments!!.getString("userId").toString()
        }

        usersRef=FirebaseDatabase.getInstance().reference.child("Users").child(currentUserId)
        postsQuery=FirebaseDatabase.getInstance().reference.child("Posts").orderByChild("uid").equalTo(currentUserId)

        imgPersonProfilePic =view.findViewById(R.id.imgPersonProfilePic)
        txtPersonProfileName=view.findViewById(R.id.txtPersonProfileName)
        //txtPersonProfileCountry=view.findViewById(R.id.txtPersonProfileCountry)
        txtPersonProfileEmail=view.findViewById(R.id.txtPersonProfileEmail)
        //txtPersonProfileContact=view.findViewById(R.id.txtPersonProfileContact)
        //txtPersonProfileDob=view.findViewById(R.id.txtPersonProfileDob)
        //txtPersonProfileGender=view.findViewById(R.id.txtPersonProfileGennder)
        //profileToolbar=view.findViewById(R.id.profileToolbar)
        recyclerProfilePosts=view.findViewById(R.id.recyclerProfilePosts)
        imgBtnEditProfile=view.findViewById(R.id.imgBtnEditProfile)
        imgBtnLogOut=view.findViewById(R.id.imgBtnLogOut)

        if (activity!=null) {
            /*(activity as AppCompatActivity).setSupportActionBar(profileToolbar)
            (activity as AppCompatActivity).supportActionBar?.title="My Profile"
            (activity as AppCompatActivity).supportActionBar?.setHomeButtonEnabled(true)
            (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)*/
            layoutManager = GridLayoutManager(activity,3)
            //layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        }

        if (activity!=null && currentUserId!=currentUserIdDuplicate){
            (activity as AppCompatActivity).supportActionBar?.title="Profile"
        } else if (currentUserId==currentUserIdDuplicate) {
            imgBtnEditProfile.visibility=View.VISIBLE
            imgBtnLogOut.visibility=View.VISIBLE
        }

        val postListener=object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                val message=error.message
                Toast.makeText(activity,"Error occurred: $message" ,Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("profileImage")) {
                        myProfileImage=snapshot.child("profileImage").value.toString()
                        Picasso.with(activity).load(myProfileImage).placeholder(R.drawable.profile).into(imgPersonProfilePic)
                    }
                    val myFullName:String = snapshot.child("name").value.toString()
                    //val myCountry: String = snapshot.child("country").value.toString()
                    val myEmail: String = snapshot.child("email").value.toString()
                    //val myContact: String = snapshot.child("phone").value.toString()
                    //val myDOB: String = snapshot.child("dob").value.toString()
                    //val myGender: String = snapshot.child("gender").value.toString()

                    txtPersonProfileName.text=myFullName
                    /*if (myCountry == "Select") {
                        val country="No Country"
                        txtPersonProfileCountry.text=country
                    } else {
                        txtPersonProfileCountry.text=myCountry
                    }*/
                    txtPersonProfileEmail.text=myEmail
                    //txtPersonProfileContact.text=myContact
                    //txtPersonProfileDob.text=myDOB
                    /*if (myGender=="Select") {
                        val gender="No Gender"
                        txtPersonProfileGender.text=gender
                    } else {
                        txtPersonProfileGender.text=myGender
                    }*/
                }
            }
        }

        usersRef.addValueEventListener(postListener)

        getAllProfilePosts()

        imgPersonProfilePic.setOnClickListener {
            if (activity!=null) {
                val builder= AlertDialog.Builder(activity)
                val postImage= RoundedImageView(activity)
                postImage.minimumHeight=600
                postImage.cornerRadius=5F
                postImage.setPaddingRelative(5,5,5,5)
                Picasso.with(activity).load(myProfileImage).into(postImage)
                builder.setView(postImage)
                builder.create()
                builder.show()
            }
        }

        imgBtnLogOut.setOnClickListener {
            val dialog=AlertDialog.Builder(activity)
            dialog.setTitle("Log Out?")
            dialog.setMessage("Are you sure want to log out?")
            dialog.setPositiveButton("Yes") { _,_ ->
                logOut()
            }
            dialog.setNegativeButton("Cancel") { _,_-> }
            dialog.create().show()
        }

        imgBtnEditProfile.setOnClickListener {
            val intent=Intent(activity, SetupActivity::class.java)
            activity?.startActivity(intent)
        }

        return view
    }

    private fun getAllProfilePosts() {
        postsQuery.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (ds in snapshot.children) {
                        val profilePost:Posts?=ds.getValue(Posts::class.java)
                        if (profilePost?.postImage!="none" && profilePost?.postImage!=null) {
                            profilePostList.add(profilePost)
                        }
                        if (activity!=null) {
                            profilePostsAdapter= ProfilePostsAdapter(activity!!,profilePostList)
                            recyclerProfilePosts.adapter=profilePostsAdapter
                            recyclerProfilePosts.layoutManager=layoutManager
                        }
                    }
                }
            }
        })
    }

    private fun logOut() {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        if (activity!=null) {
            googleSignInClient= GoogleApiClient.Builder(activity as Context)
                .enableAutoManage(activity!!, GoogleApiClient.OnConnectionFailedListener {
                    Toast.makeText(activity as Context,"Connection to google sign in failed..!!", Toast.LENGTH_LONG).show()
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
                        Toast.makeText(activity as Context,"Something went wrong",Toast.LENGTH_LONG).show()
                    }
                }

                override fun onConnectionSuspended(p0: Int) {

                }
            }
            googleSignInClient.registerConnectionCallbacks(callBacks)
        }
    }

    private fun sendUserToLoginActivity() {
        val intent=Intent(activity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        activity?.finish()
    }
}