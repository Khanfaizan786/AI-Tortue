package com.example.a_i_tortue.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a_i_tortue.R
import com.example.a_i_tortue.adapter.GroupChatAdapter
import com.example.a_i_tortue.adapter.GroupsAdapter
import com.example.a_i_tortue.model.GroupChat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class ChatActivity : AppCompatActivity() {

    private lateinit var toolbarGroupChat:Toolbar
    private lateinit var recyclerGroupChats:RecyclerView
    private lateinit var imgBackButton:ImageButton
    private lateinit var imgToolbarGroupIcon:CircleImageView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var groupChatAdapter: GroupChatAdapter
    private lateinit var etInputMessage:EditText
    private lateinit var txtToolbarGroupName:TextView
    private lateinit var sendMessageButton:ImageButton
    private lateinit var currentUserName:String
    private lateinit var currentUserImage:String
    private var groupChatList= arrayListOf<GroupChat>()
    private var groupName: String?="Group Name"
    private var groupKey:String?="Group Key"
    private var groupImage:String?="none"
    private var admin:String?="admin"
    private var postKey:String?="none"
    private lateinit var groupMsgRef:DatabaseReference
    private lateinit var groupUsersRef:DatabaseReference
    private lateinit var groupsRef:DatabaseReference
    private lateinit var usersRef:DatabaseReference
    private lateinit var currentUserId: String
    private lateinit var mAuth: FirebaseAuth
    private lateinit var saveCurrentDate: String
    private lateinit var saveCurrentTime: String
    private lateinit var postRandomName: String
    private var groupUsersList= arrayListOf<String>()
    private var count=100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        toolbarGroupChat=findViewById(R.id.toolbarGroupChat)
        recyclerGroupChats=findViewById(R.id.recyclerGroupChats)
        etInputMessage=findViewById(R.id.etInputMessage)
        sendMessageButton=findViewById(R.id.sendMessageButton)
        txtToolbarGroupName=findViewById(R.id.txtToolbarGroupName)
        imgBackButton=findViewById(R.id.imgBackButton)
        imgToolbarGroupIcon=findViewById(R.id.imgToolbarGroupIcon)

        if (intent!=null) {
            groupKey=intent.getStringExtra("groupName")
            groupName=intent.getStringExtra("name")
            groupImage=intent.getStringExtra("image")
            postKey=intent.getStringExtra("postKey")
        }
        setSupportActionBar(toolbarGroupChat)
        txtToolbarGroupName.text=groupName

        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth.currentUser?.uid.toString()
        groupMsgRef=FirebaseDatabase.getInstance().reference.child("Group Messages").child(groupKey!!)
        groupUsersRef=FirebaseDatabase.getInstance().reference.child("Group Users").child(groupKey!!)
        groupsRef=FirebaseDatabase.getInstance().reference.child("Groups").child(groupKey!!)
        usersRef=FirebaseDatabase.getInstance().reference.child("Users").child(currentUserId)

        layoutManager= LinearLayoutManager(this)

        if (groupImage!="none") {
            try {
                Picasso.with(this@ChatActivity).load(Uri.parse(groupImage)).placeholder(R.drawable.profile)
                    .into(imgToolbarGroupIcon)
            } catch (e: Exception) {
            }
        }

        if (postKey!="none" && postKey!=null) {
            etInputMessage.setText(postKey)
        }

        getGroupUsers()

        getAllMessages()

        getAdmin()

        groupUsersRef.child(currentUserId).setValue(true)

        usersRef.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                val message= error.message
                Toast.makeText(this@ChatActivity, "Error occurred: $message", Toast.LENGTH_SHORT).show()
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

        recyclerGroupChats.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom<oldBottom) {
                recyclerGroupChats.postDelayed(Runnable {
                    if (groupChatList.size>0) {
                        groupChatAdapter = GroupChatAdapter(this@ChatActivity,groupChatList,groupKey!!)
                        recyclerGroupChats.scrollToPosition(groupChatAdapter.itemCount-1)
                    }
                },100)
            }
        }

        sendMessageButton.setOnClickListener {
            val message=etInputMessage.text.toString()
            if (TextUtils.isEmpty(message)) {
                Toast.makeText(this@ChatActivity,"Please Type a message to send",Toast.LENGTH_SHORT).show()
            } else {
                etInputMessage.setText("")
                sendMessage(message)
            }
        }

        imgBackButton.setOnClickListener {
            super.onBackPressed()
        }
    }

    private fun getAdmin() {
        groupsRef.child("admin").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    admin=snapshot.value.toString()
                }
            }
        })
    }

    private fun getAllMessages() {
        groupMsgRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                val message= error.message
                Toast.makeText(this@ChatActivity, "Error occurred: $message", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                groupChatList.clear()
                if (snapshot.hasChildren()) {
                    for (ds in snapshot.children) {
                        val groupChat:GroupChat?=ds.getValue(GroupChat::class.java)
                        if (groupChat!=null) {
                            groupChatList.add(groupChat)
                        }
                        groupChatAdapter = GroupChatAdapter(this@ChatActivity,groupChatList,groupKey!!)
                        groupChatAdapter.notifyDataSetChanged()
                        recyclerGroupChats.adapter = groupChatAdapter
                        recyclerGroupChats.layoutManager=layoutManager
                    }
                    recyclerGroupChats.scrollToPosition(groupChatAdapter.itemCount-1)
                } else {
                    groupChatAdapter = GroupChatAdapter(this@ChatActivity,groupChatList,groupKey!!)
                    groupChatAdapter.notifyDataSetChanged()
                    recyclerGroupChats.adapter = groupChatAdapter
                    recyclerGroupChats.layoutManager=layoutManager
                }
            }
        })
    }

    private fun sendMessage(message: String) {
        val userMessageKey: DatabaseReference = groupMsgRef.push()
        val messagePushId: String? =userMessageKey.key

        val callForDate = Calendar.getInstance()
        val currentDate = SimpleDateFormat("yyyy-MM-dd")
        saveCurrentDate = currentDate.format(callForDate.time)

        val currentDate2=SimpleDateFormat("dd MMM yyyy")
        val chatDate=currentDate2.format(callForDate.time)

        val callForTime = Calendar.getInstance()
        val currentTime = SimpleDateFormat("HH:mm:ss")
        saveCurrentTime = currentTime.format(callForTime.time)

        val currentTime2 = SimpleDateFormat("hh:mm aa")
        val chatTime = currentTime2.format(callForTime.time)

        postRandomName = saveCurrentDate + saveCurrentTime

        val type = if ((postKey!="none" && postKey!=null) && postKey==message) {
            "postKey"
        } else {
            "text"
        }

        val messageMap=HashMap<String,String>()
        messageMap["message"] = message
        messageMap["time"] = chatTime
        messageMap["date"] = chatDate
        messageMap["type"] = type
        messageMap["timeStamp"] = postRandomName
        messageMap["from"] = currentUserId
        messageMap["image"] = currentUserImage
        messageMap["name"] = currentUserName

        groupMsgRef.child(messagePushId!!).updateChildren(messageMap as Map<String, Any>).addOnSuccessListener {
            updateRecentMessage(message)
        }.addOnFailureListener {
            val message= it.message
            Toast.makeText(this@ChatActivity, "Error occured: $message", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateRecentMessage(message: String) {
        val messageMap=HashMap<String,String>()
        messageMap["recentMessage"]=message
        messageMap["timeStamp"]=postRandomName
        groupsRef.updateChildren(messageMap as Map<String, Any>).addOnSuccessListener {
            updateGroupUsers()
        }.addOnFailureListener {
            val message= it.message
            Toast.makeText(this@ChatActivity, "Error occurred: $message", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateGroupUsers() {
        if (groupUsersList.size!=0) {
            for (user in groupUsersList) {
                if (user!=currentUserId) {
                    groupUsersRef.child(user).setValue(false)
                }
            }
        }
    }

    private fun getGroupUsers() {
        groupUsersRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                val message= error.message
                Toast.makeText(this@ChatActivity, "Error occurred: $message", Toast.LENGTH_SHORT).show()
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                groupUsersList.clear()
                for (ds in snapshot.children) {
                    groupUsersList.add(ds.key.toString())
                }
                if (!groupUsersList.contains(currentUserId) && groupKey!= "Group Key") {
                    count=200
                    finish()
                }
            }
        })
    }

    override fun onBackPressed() {
        if (count==100) {
            groupUsersRef.child(currentUserId).setValue(true)
        }
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        val inflater:MenuInflater=menuInflater
        inflater.inflate(R.menu.group_chat_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            R.id.deleteGroupConversation -> {

                if (admin==currentUserId) {
                    val builder=AlertDialog.Builder(this@ChatActivity)
                    builder.setTitle("Delete for everyone ?")
                    builder.setMessage("This will delete conversation for all members.")
                    builder.setPositiveButton("Delete") { _,_->
                        deleteConversation()
                    }
                    builder.setNegativeButton("Cancel"){ _,_-> }
                    builder.create().show()
                } else {
                    val builder=AlertDialog.Builder(this@ChatActivity)
                    builder.setTitle("Permission Denied !!")
                    builder.setMessage("Only admins can delete the conversation for everyone.")
                    builder.setPositiveButton("Ok") { _,_-> }
                    builder.create().show()
                }
            }

            R.id.groupInfo -> {

                val intent=Intent(this@ChatActivity,GroupInfoDisplayActivity::class.java)
                intent.putExtra("groupKey",groupKey)
                intent.putExtra("groupName",groupName)
                intent.putExtra("groupIcon",groupImage)
                intent.putExtra("participantsList",groupUsersList)
                intent.putExtra("admin",admin)
                startActivity(intent)
            }

            R.id.goToFirstGroupMessage -> {
                if (groupChatList.size>0) {
                    groupChatAdapter = GroupChatAdapter(this@ChatActivity,groupChatList,groupKey!!)
                    recyclerGroupChats.smoothScrollToPosition(0)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteConversation() {
        groupMsgRef.removeValue()

        val callForDate = Calendar.getInstance()
        val currentDate = SimpleDateFormat("yyyy-MM-dd")
        saveCurrentDate = currentDate.format(callForDate.time)

        val callForTime = Calendar.getInstance()
        val currentTime = SimpleDateFormat("HH:mm:ss")
        saveCurrentTime = currentTime.format(callForTime.time)

        postRandomName = saveCurrentDate + saveCurrentTime

        updateRecentMessage("Empty Conversation")
        if (groupUsersList.size!=0) {
            for (user in groupUsersList) {
                if (user!=currentUserId) {
                    groupUsersRef.child(user).setValue(true)
                }
            }
        }
        getAllMessages()
    }
}
