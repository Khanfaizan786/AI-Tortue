package com.example.a_i_tortue.fragment

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.a_i_tortue.R
import com.example.a_i_tortue.activity.MessageActivity
import com.example.a_i_tortue.adapter.ChatAdapter
import com.example.a_i_tortue.adapter.GroupsAdapter
import com.example.a_i_tortue.adapter.SingleChatAdapter
import com.example.a_i_tortue.model.Group
import com.example.a_i_tortue.model.SingleChat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import java.util.*
import kotlin.Comparator

class ChatFragment : Fragment() {

    private lateinit var recyclerChats: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var rlChatFragment:RelativeLayout
    private lateinit var progressBarChatFragment:ProgressBar
    private lateinit var chatRef: DatabaseReference
    private lateinit var currentUserId: String
    private lateinit var mAuth: FirebaseAuth
    private var chatList= arrayListOf<SingleChat>()
    private lateinit var singleChatAdapter: ChatAdapter
    private var postKey:String?="none"

    private var timeComparator=Comparator<SingleChat>{chat1,chat2 ->
        chat1.timeStamp.compareTo(chat2.timeStamp,true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view=inflater.inflate(R.layout.fragment_chat, container, false)

        if (activity!=null) {
            val messageActivity=activity as MessageActivity
            postKey=messageActivity.sendData()
        }

        setHasOptionsMenu(true)

        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth.currentUser?.uid.toString()
        chatRef= FirebaseDatabase.getInstance().reference.child("Chat List").child(currentUserId)

        recyclerChats=view.findViewById(R.id.recyclerChats)
        rlChatFragment=view.findViewById(R.id.rlChatFragment)
        progressBarChatFragment=view.findViewById(R.id.progressBarChatFragment)
        layoutManager= LinearLayoutManager(activity)

        rlChatFragment.visibility=View.VISIBLE
        progressBarChatFragment.visibility=View.VISIBLE

        getAllChatList()

        return view
    }

    private fun getAllChatList() {
        chatRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                val message=error.message
                if (activity!=null) {
                    Toast.makeText(activity,"Error occurred: $message", Toast.LENGTH_LONG).show()
                }
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChildren()){
                    chatList.clear()
                    for (ds in snapshot.children) {
                        val singleChat:SingleChat?=ds.getValue(SingleChat::class.java)
                        if (singleChat?.recentMessage != null) {
                            chatList.add(singleChat)
                        }
                    }
                    Collections.sort(chatList,timeComparator)
                    chatList.reverse()
                    if (activity!=null) {
                        singleChatAdapter = ChatAdapter(activity as Context,chatList,rlChatFragment,progressBarChatFragment,postKey)
                        singleChatAdapter.notifyDataSetChanged()
                        recyclerChats.adapter = singleChatAdapter
                        recyclerChats.layoutManager=layoutManager
                    }
                } else {
                    rlChatFragment.visibility=View.GONE
                    progressBarChatFragment.visibility=View.GONE
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        if (postKey=="none" || postKey==null) {
            inflater.inflate(R.menu.chat_menu,menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId==R.id.action_add_contact) {
            showAllUsers()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAllUsers() {
        val fragment= AllUsersFragment()
        val transaction=activity?.supportFragmentManager?.beginTransaction()
        transaction?.replace(R.id.frame2,fragment)
        transaction?.commit()
    }

}
