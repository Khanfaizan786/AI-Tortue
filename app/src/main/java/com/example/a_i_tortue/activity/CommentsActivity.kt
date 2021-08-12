package com.example.a_i_tortue.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a_i_tortue.R
import com.example.a_i_tortue.adapter.CommentsAdapter
import com.example.a_i_tortue.model.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class CommentsActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserId:String
    private lateinit var toolbarComment: Toolbar
    private lateinit var recyclerComments: RecyclerView
    private lateinit var etInputComment: EditText
    private lateinit var postCommentButton: ImageButton
    private lateinit var commentsRef: DatabaseReference
    private lateinit var usersRef:DatabaseReference
    private var postCommentsList= arrayListOf<Comment>()
    private lateinit var commentsAdapter: CommentsAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var postKey:String?="postKey"
    private var position:Int?=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        toolbarComment=findViewById(R.id.toolbarComment)
        recyclerComments=findViewById(R.id.recyclerComments)
        etInputComment=findViewById(R.id.etInputComment)
        postCommentButton=findViewById(R.id.postCommentButton)
        layoutManager=LinearLayoutManager(this)

        setSupportActionBar(toolbarComment)
        supportActionBar?.title="Comments"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mAuth=FirebaseAuth.getInstance()
        currentUserId=mAuth.currentUser?.uid.toString()
        usersRef=FirebaseDatabase.getInstance().reference.child("Users").child(currentUserId)

        if (intent!=null) {
            postKey=intent.getStringExtra("postKey")
        }

        commentsRef=FirebaseDatabase.getInstance().reference.child("Posts").child(postKey!!).child("Comments")

        getAllComments()

        recyclerComments.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom<oldBottom) {
                recyclerComments.postDelayed({
                    if (postCommentsList.size>0) {
                        commentsAdapter= CommentsAdapter(this@CommentsActivity,postCommentsList)
                        recyclerComments.smoothScrollToPosition(commentsAdapter.itemCount)
                    }
                },100)
            }
        }

        postCommentButton.setOnClickListener {

            val commentMsg=etInputComment.text.toString()

            if (!TextUtils.isEmpty(commentMsg)) {
                etInputComment.setText("")
                usersRef.addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {

                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            var image="none"
                            if (snapshot.hasChild("profileImage")) {
                                image=snapshot.child("profileImage").value.toString()
                            }
                            val name=snapshot.child("name").value.toString()
                            postComment(name,image,commentMsg)
                        }
                    }
                })
            }
        }
    }

    private fun getAllComments() {
        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChildren()) {
                    postCommentsList.clear()
                    for (ds in snapshot.children) {
                        val comment:Comment?=ds.getValue(Comment::class.java)
                        if (comment!=null) {
                            postCommentsList.add(comment)
                        }
                    }
                    commentsAdapter= CommentsAdapter(this@CommentsActivity,postCommentsList)
                    commentsAdapter.notifyDataSetChanged()
                    recyclerComments.adapter=commentsAdapter
                    recyclerComments.layoutManager=layoutManager
                    recyclerComments.smoothScrollToPosition(commentsAdapter.itemCount)
                }
            }
        })
    }

    private fun postComment(name: String, image: String, commentMsg: String) {
        val userCommentKey: DatabaseReference = commentsRef.push()
        val commentPushId=userCommentKey.key

        val callForDate = Calendar.getInstance()
        val currentDate2= SimpleDateFormat("dd MMM yyyy")
        val chatDate=currentDate2.format(callForDate.time)

        val callForTime = Calendar.getInstance()
        val currentTime2 = SimpleDateFormat("hh:mm aa")
        val chatTime = currentTime2.format(callForTime.time)

        val commentsMap=HashMap<String,String>()
        commentsMap["comment"]=commentMsg
        commentsMap["image"]=image
        commentsMap["name"]=name
        commentsMap["time"]=chatTime
        commentsMap["date"]=chatDate

        commentsRef.child(commentPushId!!).setValue(commentsMap)
    }
}
