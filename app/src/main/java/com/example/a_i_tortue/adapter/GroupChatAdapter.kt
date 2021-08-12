package com.example.a_i_tortue.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.a_i_tortue.R
import com.example.a_i_tortue.activity.MainActivity
import com.example.a_i_tortue.model.GroupChat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class GroupChatAdapter(private val context: Context, private val groupChatList:ArrayList<GroupChat>, groupKey:String):RecyclerView.Adapter<GroupChatAdapter.GroupChatViewHolder>(){

    private val groupMsgRef: DatabaseReference= FirebaseDatabase.getInstance().reference.child("Group Messages").child(groupKey)
    private val currentUserId:String= FirebaseAuth.getInstance().currentUser?.uid.toString()

    class GroupChatViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val messageProfileImage:CircleImageView=view.findViewById(R.id.messageProfileImage)
        val receiverMessageText: TextView=view.findViewById(R.id.receiverMessageText)
        val senderMessageText:TextView=view.findViewById(R.id.sender_message_text)
        val msgSenderName:TextView=view.findViewById(R.id.msgSenderName)
        val llMessageSenderLayout:LinearLayout=view.findViewById(R.id.llMessageSenderLayout)
        val txtGroupChatDate: TextView =view.findViewById(R.id.txtGroupChatDate)
        val groupMsgReceiverTime: TextView =view.findViewById(R.id.groupMsgReceiverTime)
        val groupMsgSenderTime: TextView =view.findViewById(R.id.groupMsgSenderTime)
        val llMessageReceiverLayout:LinearLayout=view.findViewById(R.id.llMessageReceiverLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupChatViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.all_chats_layout, parent,false)
        return GroupChatViewHolder(view)
    }

    override fun getItemCount(): Int {
        return groupChatList.size
    }

    override fun onBindViewHolder(holder: GroupChatViewHolder, position: Int) {
        val groupChat=groupChatList[position]
        holder.messageProfileImage.visibility=View.INVISIBLE
        holder.llMessageReceiverLayout.visibility=View.GONE
        holder.llMessageSenderLayout.visibility=View.GONE
        holder.txtGroupChatDate.visibility=View.GONE
        holder.msgSenderName.visibility=View.GONE

        holder.receiverMessageText.setTextColor(context.resources.getColor(R.color.colorWhite))

        if (groupChat.from!=currentUserId) {
            holder.llMessageSenderLayout.visibility=View.VISIBLE
            holder.receiverMessageText.text=groupChat.message
            holder.groupMsgSenderTime.text=groupChat.time
            if (groupChat.type=="postKey") {
                holder.receiverMessageText.setTextColor(context.resources.getColor(R.color.colorPostKey))
            }
            if (groupChat.image!="none") {
                holder.messageProfileImage.visibility=View.VISIBLE
                holder.msgSenderName.visibility=View.VISIBLE
                holder.msgSenderName.text=groupChat.name
                try {
                    Picasso.with(context).load(Uri.parse(groupChat.image)).placeholder(R.drawable.profile)
                        .into(holder.messageProfileImage)
                } catch (e: Exception) {

                holder.messageProfileImage.visibility=View.VISIBLE
                holder.msgSenderName.visibility=View.VISIBLE
                holder.msgSenderName.text=groupChat.name
                try {
                    Picasso.with(context).load(Uri.parse(groupChat.image)).placeholder(R.drawable.profile)
                        .into(holder.messageProfileImage)
                } catch (e: Exception) {
                }
            }
        }
        } else {
            holder.llMessageReceiverLayout.visibility=View.VISIBLE
            holder.senderMessageText.text=groupChat.message
            holder.groupMsgReceiverTime.text=groupChat.time
            if (groupChat.type=="postKey") {
                holder.senderMessageText.setTextColor(context.resources.getColor(R.color.colorPostKey))
            }
        }

        if (position<=0) {
            holder.txtGroupChatDate.visibility=View.VISIBLE
            holder.txtGroupChatDate.text=groupChat.date
        } else if (groupChat.date!=groupChatList[position-1].date) {
            holder.txtGroupChatDate.visibility=View.VISIBLE
            holder.txtGroupChatDate.text=groupChat.date
        }

        holder.llMessageSenderLayout.setOnLongClickListener {
            val builder=AlertDialog.Builder(context)
            builder.setMessage("Do you really want to delete message ?")
            builder.setTitle("Delete message")
            builder.setPositiveButton("Delete"){ _ ,_->
                groupMsgRef.orderByChild("timeStamp").equalTo(groupChat.timeStamp).ref.removeValue()
            }
            builder.setNegativeButton("Cancel") { _,_->}
            builder.create().show()

            return@setOnLongClickListener true
        }

        holder.senderMessageText.setOnClickListener {
            if (groupChat.type=="postKey") {
                goToPost(groupChat.message)
            }
        }

        holder.receiverMessageText.setOnClickListener {
            if (groupChat.type=="postKey") {
                goToPost(groupChat.message)
            }
        }

        holder.senderMessageText.setOnLongClickListener {
            var key:String?=null
            val builder=AlertDialog.Builder(context)
            builder.setMessage("Do you really want to delete message ?")
            builder.setTitle("Delete message")
            builder.setPositiveButton("Delete"){ _ ,_->
                groupMsgRef.orderByChild("timeStamp").equalTo(groupChat.timeStamp).addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {

                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (ds in snapshot.children) {
                            key=ds.key
                        }
                        if (key!=null) {
                            Toast.makeText(context,key,Toast.LENGTH_SHORT).show()
                            groupMsgRef.child(key!!).removeValue()
                        }
                    }
                })

            }
            builder.setNegativeButton("Cancel") { _,_->}
            builder.create().show()

            return@setOnLongClickListener true
        }
    }

    private fun goToPost(message: String?) {
        val intent= Intent(context, MainActivity::class.java)
        intent.putExtra("from","ChatActivity")
        intent.putExtra("userId","none")
        intent.putExtra("postKey",message)
        context.startActivity(intent)
    }
}