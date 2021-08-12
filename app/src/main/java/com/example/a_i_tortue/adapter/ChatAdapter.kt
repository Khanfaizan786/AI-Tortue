package com.example.a_i_tortue.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.a_i_tortue.R
import com.example.a_i_tortue.activity.ChatActivity
import com.example.a_i_tortue.activity.SingleChatActivity
import com.example.a_i_tortue.model.Group
import com.example.a_i_tortue.model.SingleChat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapter (
    val context: Context,
    private val chatList:ArrayList<SingleChat>,
    private val rlChatFragment:RelativeLayout,
    private val progressBarChatFragment:ProgressBar,
    private val postKey:String?
) : RecyclerView.Adapter<ChatAdapter.ChatHolder>() {

    private val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")
    private val currentUserId:String= FirebaseAuth.getInstance().currentUser?.uid.toString()

    class ChatHolder(view: View): RecyclerView.ViewHolder(view) {
        val txtGroupName: TextView =view.findViewById(R.id.txtGroupName)
        val imgGroupIcon: CircleImageView =view.findViewById(R.id.imgGroupIcon)
        val imgShowUnseenMsg: CircleImageView =view.findViewById(R.id.imgShowUnseenMsg)
        val llGroupLayout: RelativeLayout =view.findViewById(R.id.llGroupLayout)
        val txtLastMessage: TextView =view.findViewById(R.id.txtLastMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.all_groups_layout, parent,false)
        return ChatHolder(view)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {
        val singleChat=chatList[position]

        holder.txtLastMessage.typeface = Typeface.DEFAULT
        holder.imgShowUnseenMsg.visibility= View.GONE

        var name:String="none"
        var image:String="none"

        holder.txtLastMessage.text=singleChat.recentMessage

        if (singleChat.uid!=null) {
            usersRef.child(singleChat.uid!!).addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        val message=error.message
                        Toast.makeText(context,"Error occurred: $message", Toast.LENGTH_LONG).show()
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            name=snapshot.child("name").value.toString()
                            holder.txtGroupName.text=name
                            if (snapshot.hasChild("profileImage")) {
                                image=snapshot.child("profileImage").value.toString()
                            }
                        }
                        if (image!="none") {
                            try {
                                Picasso.with(context).load(Uri.parse(image)).placeholder(R.drawable.profile)
                                    .into(holder.imgGroupIcon)
                            } catch (e: Exception) {
                            }
                        }
                        if (name!="none") {
                            rlChatFragment.visibility=View.GONE
                            progressBarChatFragment.visibility=View.GONE
                        }
                    }
                })
        }

        if (singleChat.seen=="false") {
            holder.txtLastMessage.typeface = Typeface.DEFAULT_BOLD
            holder.imgShowUnseenMsg.visibility= View.VISIBLE
        }

        holder.llGroupLayout.setOnClickListener {
            val intent= Intent(context, SingleChatActivity::class.java)
            intent.putExtra("name",name)
            intent.putExtra("uid",singleChat.uid)
            intent.putExtra("image",image)
            intent.putExtra("postKey",postKey)
            intent.putExtra("from","ChatFragment")
            context.startActivity(intent)
        }

        holder.imgGroupIcon.setOnClickListener {
            val builder= AlertDialog.Builder(context)
            val postImage= ImageView(context)
            postImage.minimumHeight=600
            postImage.setPaddingRelative(5,5,5,5)
            Picasso.with(context).load(image).placeholder(R.drawable.profile).into(postImage)
            builder.setView(postImage)
            builder.show()
        }
    }
}