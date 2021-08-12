package com.example.a_i_tortue.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a_i_tortue.R
import com.example.a_i_tortue.adapter.SingleChatAdapter
import com.example.a_i_tortue.model.GroupChat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class SingleChatActivity : AppCompatActivity() {

    private var profileImage:String?="none"
    private var userName:String?="UserName"
    private var userId:String?="uid"
    private var from:String?="activity"

    private lateinit var toolbarSingleChat: Toolbar
    private lateinit var recyclerSingleChats: RecyclerView
    private lateinit var imgSingleChatBackButton: ImageButton
    private lateinit var imgToolbarUserIcon: CircleImageView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var singleChatAdapter: SingleChatAdapter
    private lateinit var etInputSingleChatMessage: EditText
    private lateinit var txtToolbarUserName: TextView
    private lateinit var sendSingleMessageButton: ImageButton
    private lateinit var currentUserName:String
    private lateinit var currentUserImage:String
    private var singleChatList= arrayListOf<GroupChat>()
    private lateinit var senderMsgRef: DatabaseReference
    private lateinit var senderRef: DatabaseReference
    private lateinit var usersRef: DatabaseReference
    private lateinit var currentUserId: String
    private lateinit var mAuth: FirebaseAuth
    private lateinit var saveCurrentDate: String
    private lateinit var saveCurrentTime: String
    private lateinit var postRandomName: String
    private var postKey:String?="none"

    private var notify:Boolean=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_chat)

        toolbarSingleChat=findViewById(R.id.toolbarSingleChat)
        recyclerSingleChats=findViewById(R.id.recyclerSingleChats)
        etInputSingleChatMessage=findViewById(R.id.etInputSingleChatMessage)
        sendSingleMessageButton=findViewById(R.id.sendSingleMessageButton)
        txtToolbarUserName=findViewById(R.id.txtToolbarUserName)
        imgSingleChatBackButton=findViewById(R.id.imgSingleChatBackButton)
        imgToolbarUserIcon=findViewById(R.id.imgToolbarUserIcon)

        layoutManager= LinearLayoutManager(this)

        if (intent!=null) {
            profileImage=intent.getStringExtra("image")
            userName=intent.getStringExtra("name")
            userId=intent.getStringExtra("uid")
            from=intent.getStringExtra("from")
            postKey=intent.getStringExtra("postKey")
        }

        if (postKey!="none") {
            etInputSingleChatMessage.setText(postKey)
        }

        setSupportActionBar(toolbarSingleChat)
        txtToolbarUserName.text=userName

        if (profileImage!="none") {
            try {
                Picasso.with(this@SingleChatActivity).load(Uri.parse(profileImage)).placeholder(R.drawable.profile)
                    .into(imgToolbarUserIcon)
            } catch (e: Exception) {
            }
        }

        mAuth=FirebaseAuth.getInstance()
        currentUserId=mAuth.currentUser?.uid.toString()
        senderMsgRef=FirebaseDatabase.getInstance().reference.child("User Messages")
        senderRef=FirebaseDatabase.getInstance().reference.child("Chat List")
        usersRef=FirebaseDatabase.getInstance().reference.child("Users").child(currentUserId)

        if (from=="ChatFragment") {
            senderRef.child(currentUserId).child(userId!!).child("seen").setValue("true")
        }

        usersRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                val message= error.message
                Toast.makeText(this@SingleChatActivity, "Error occurred: $message", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    currentUserImage = if (snapshot.hasChild("profileImage")) {
                        snapshot.child("profileImage").value.toString()
                    } else {
                        "none"
                    }
                    currentUserName=snapshot.child("name").value.toString()
                }
            }
        })

        getAllMessages()

        recyclerSingleChats.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom<oldBottom) {
                recyclerSingleChats.postDelayed(Runnable {
                    if (singleChatList.size>0) {
                        singleChatAdapter = SingleChatAdapter(this@SingleChatActivity,singleChatList)
                        recyclerSingleChats.scrollToPosition(singleChatAdapter.itemCount-1)
                    }
                },100)
            }
        }

        sendSingleMessageButton.setOnClickListener {
            val message=etInputSingleChatMessage.text.toString()
            if (TextUtils.isEmpty(message)) {
                Toast.makeText(this@SingleChatActivity,"Please Type a message to send", Toast.LENGTH_SHORT).show()
            } else {
                notify=true
                etInputSingleChatMessage.setText("")
                sendMessage(message)
            }
        }
    }

    private fun getAllMessages() {
        senderMsgRef.child(currentUserId).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                val message= error.message
                Toast.makeText(this@SingleChatActivity, "Error occurred: $message", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild(userId!!)) {
                    singleChatList.clear()
                    for (ds in snapshot.child(userId!!).children) {
                        val singleChat:GroupChat?=ds.getValue(GroupChat::class.java)
                        if (singleChat!=null) {
                            singleChatList.add(singleChat)
                        }
                        singleChatAdapter = SingleChatAdapter(this@SingleChatActivity,singleChatList)
                        singleChatAdapter.notifyDataSetChanged()
                        recyclerSingleChats.adapter = singleChatAdapter
                        recyclerSingleChats.layoutManager=layoutManager
                    }
                    recyclerSingleChats.scrollToPosition(singleChatAdapter.itemCount-1)
                }
            }
        })
    }

    private fun sendMessage(message: String) {
        val userMessageKey: DatabaseReference = senderMsgRef.child(currentUserId).child(userId!!).push()
        val messagePushId: String? =userMessageKey.key

        val callForDate = Calendar.getInstance()
        val currentDate = SimpleDateFormat("yyyy-MM-dd")
        saveCurrentDate = currentDate.format(callForDate.time)

        val callForTime = Calendar.getInstance()
        val currentTime = SimpleDateFormat("HH:mm:ss")
        saveCurrentTime = currentTime.format(callForTime.time)

        postRandomName = saveCurrentDate + saveCurrentTime

        val currentDate2=SimpleDateFormat("dd MMM yyyy")
        val chatDate=currentDate2.format(callForDate.time)

        val currentTime2 = SimpleDateFormat("hh:mm aa")
        val chatTime = currentTime2.format(callForTime.time)

        val type = if ((postKey!="none" && postKey!=null) && postKey==message) {
            "postKey"
        } else {
            "text"
        }

        val messageMap=HashMap<String,String>()
        messageMap["message"]=message
        messageMap["time"]=chatTime
        messageMap["date"]=chatDate
        messageMap["type"]=type
        messageMap["timeStamp"] = postRandomName
        messageMap["from"]=currentUserId
        messageMap["image"]=currentUserImage
        messageMap["name"]=currentUserName

        senderMsgRef.child(currentUserId).child(userId!!).child(messagePushId!!).updateChildren(messageMap as Map<String, Any>).addOnFailureListener {
            val message= it.message
            Toast.makeText(this@SingleChatActivity, "Error occurred: $message", Toast.LENGTH_SHORT).show()
        }
        senderMsgRef.child(userId!!).child(currentUserId).child(messagePushId).updateChildren(messageMap as Map<String, Any>).addOnFailureListener {
            val message= it.message
            Toast.makeText(this@SingleChatActivity, "Error occurred: $message", Toast.LENGTH_SHORT).show()
        }

        val lastMessageSender=HashMap<String,String>()
        lastMessageSender["timeStamp"]=postRandomName
        lastMessageSender["recentMessage"]=message
        lastMessageSender["uid"]=userId!!
        lastMessageSender["seen"]="true"

        val lastMessageReceiver=HashMap<String,String>()
        lastMessageReceiver["timeStamp"]=postRandomName
        lastMessageReceiver["recentMessage"]=message
        lastMessageReceiver["uid"]=currentUserId
        lastMessageReceiver["seen"]="false"

        senderRef.child(currentUserId).child(userId!!).updateChildren(lastMessageSender as Map<String, Any>).addOnFailureListener {
            val message= it.message
            Toast.makeText(this@SingleChatActivity, "Error occurred: $message", Toast.LENGTH_SHORT).show()
        }
        senderRef.child(userId!!).child(currentUserId).updateChildren(lastMessageReceiver as Map<String, Any>).addOnFailureListener {
            val message= it.message
            Toast.makeText(this@SingleChatActivity, "Error occurred: $message", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        if (from=="ChatFragment") {
            senderRef.child(currentUserId).child(userId!!).child("seen").setValue("true")
        }
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        menuInflater.inflate(R.menu.single_chat_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.clearChat ->{
                val builder=AlertDialog.Builder(this@SingleChatActivity)
                builder.setMessage("Delete all messages ?")
                builder.setPositiveButton("Delete") { _,_->
                    deleteAllMessages()
                }
                builder.setNegativeButton("Cancel") { _,_-> }
                builder.create().show()
            }
            R.id.viewProfile -> {
                val intent=Intent(this@SingleChatActivity,MainActivity::class.java)
                intent.putExtra("from","SingleChatActivity")
                intent.putExtra("userId",userId)
                intent.putExtra("postKey","none")
                startActivity(intent)
            }
            R.id.goToFirstMessage -> {
                if (singleChatList.size>0) {
                    singleChatAdapter = SingleChatAdapter(this@SingleChatActivity,singleChatList)
                    recyclerSingleChats.smoothScrollToPosition(0)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteAllMessages() {
        senderMsgRef.child(currentUserId).child(userId!!).removeValue()
        senderRef.child(currentUserId).child(userId!!).removeValue()
        sendUserToMessageActivity()
    }

    private fun sendUserToMessageActivity() {
        val intent=Intent(this@SingleChatActivity,MessageActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
