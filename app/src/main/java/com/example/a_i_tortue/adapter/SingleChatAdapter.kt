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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class SingleChatAdapter(private val context:Context,private val singleChatList: ArrayList<GroupChat>):RecyclerView.Adapter<SingleChatAdapter.SingleChatViewHolder>() {

    private val currentUserId:String= FirebaseAuth.getInstance().currentUser?.uid.toString()

    class SingleChatViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val messageProfileImage: CircleImageView =view.findViewById(R.id.messageProfileImage)
        val receiverMessageText: TextView =view.findViewById(R.id.receiverMessageText)
        val senderMessageText: TextView =view.findViewById(R.id.sender_message_text)
        val msgSenderName: TextView =view.findViewById(R.id.msgSenderName)
        val llMessageSenderLayout: LinearLayout =view.findViewById(R.id.llMessageSenderLayout)
        val txtGroupChatDate: TextView =view.findViewById(R.id.txtGroupChatDate)
        val groupMsgReceiverTime: TextView =view.findViewById(R.id.groupMsgReceiverTime)
        val groupMsgSenderTime: TextView =view.findViewById(R.id.groupMsgSenderTime)
        val llMessageReceiverLayout: LinearLayout =view.findViewById(R.id.llMessageReceiverLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleChatViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.all_chats_layout, parent,false)
        return SingleChatViewHolder(view)
    }

    override fun getItemCount(): Int {
        return singleChatList.size
    }

    override fun onBindViewHolder(holder: SingleChatViewHolder, position: Int) {
        val singleChat=singleChatList[position]
        holder.msgSenderName.visibility= View.GONE
        holder.messageProfileImage.visibility= View.GONE
        holder.llMessageReceiverLayout.visibility= View.GONE
        holder.llMessageSenderLayout.visibility= View.GONE
        holder.txtGroupChatDate.visibility= View.GONE

        if (singleChat.from!=currentUserId) {
            holder.messageProfileImage.visibility= View.VISIBLE
            holder.llMessageSenderLayout.visibility= View.VISIBLE
            holder.receiverMessageText.text=singleChat.message
            holder.groupMsgSenderTime.text=singleChat.time
            holder.receiverMessageText.setTextColor(context.resources.getColor(R.color.colorWhite))
            if (singleChat.type=="postKey") {
                holder.receiverMessageText.setTextColor(context.resources.getColor(R.color.colorPostKey))
            }
            if (singleChat.image!="none") {
                try {
                    Picasso.with(context).load(Uri.parse(singleChat.image)).placeholder(R.drawable.profile)
                        .into(holder.messageProfileImage)
                } catch (e: Exception) {
                }
            }
        } else {
            holder.llMessageReceiverLayout.visibility= View.VISIBLE
            holder.senderMessageText.text=singleChat.message
            holder.groupMsgReceiverTime.text=singleChat.time
            if (singleChat.type=="postKey") {
                holder.senderMessageText.setTextColor(context.resources.getColor(R.color.colorPostKey))
            }
        }

        if (position<=0) {
            holder.txtGroupChatDate.visibility= View.VISIBLE
            holder.txtGroupChatDate.text=singleChat.date
        } else if (singleChat.date!=singleChatList[position-1].date) {
            holder.txtGroupChatDate.visibility= View.VISIBLE
            holder.txtGroupChatDate.text=singleChat.date
        }

        holder.receiverMessageText.setOnClickListener {
            if (singleChat.type=="postKey") {
                goToPost(singleChat.message)
            }
        }

        holder.senderMessageText.setOnClickListener {
            if (singleChat.type=="postKey") {
                goToPost(singleChat.message)
            }
        }
    }

    private fun goToPost(message: String?) {
        val intent=Intent(context,MainActivity::class.java)
        intent.putExtra("from","ChatActivity")
        intent.putExtra("userId","none")
        intent.putExtra("postKey",message)
        context.startActivity(intent)
    }
}