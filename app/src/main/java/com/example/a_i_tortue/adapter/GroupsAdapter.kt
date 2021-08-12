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
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.a_i_tortue.R
import com.example.a_i_tortue.activity.ChatActivity
import com.example.a_i_tortue.model.Document
import com.example.a_i_tortue.model.Group
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class GroupsAdapter(
    val context: Context,
    private val groupList:ArrayList<Group>,
    private val rlGroupFragment:RelativeLayout,
    private val progressBarGroupFragment:ProgressBar,
    private val postKey:String
) : RecyclerView.Adapter<GroupsAdapter.GroupsHolder>() {

    private val groupUsersRef: DatabaseReference= FirebaseDatabase.getInstance().reference.child("Group Users")
    private val currentUserId:String=FirebaseAuth.getInstance().currentUser?.uid.toString()

    class GroupsHolder(view: View): RecyclerView.ViewHolder(view) {
        val txtGroupName: TextView =view.findViewById(R.id.txtGroupName)
        val imgGroupIcon:CircleImageView =view.findViewById(R.id.imgGroupIcon)
        val imgShowUnseenMsg:CircleImageView =view.findViewById(R.id.imgShowUnseenMsg)
        val llGroupLayout:RelativeLayout=view.findViewById(R.id.llGroupLayout)
        val txtLastMessage: TextView =view.findViewById(R.id.txtLastMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.all_groups_layout, parent,false)
        return GroupsHolder(view)
    }

    override fun getItemCount(): Int {
        return groupList.size
    }

    override fun onBindViewHolder(holder: GroupsHolder, position: Int) {
        val group=groupList[position]
        holder.txtGroupName.text=group.name
        holder.txtLastMessage.text=group.recentMessage
        if (group.image!="none") {
            try {
                Picasso.with(context).load(Uri.parse(group.image)).placeholder(R.drawable.profile)
                    .into(holder.imgGroupIcon)
            } catch (e: Exception) {
            }
        }
        holder.txtLastMessage.typeface = Typeface.DEFAULT
        holder.imgShowUnseenMsg.visibility=View.GONE

        groupUsersRef.child(group.randomName+group.firstName).child(currentUserId)
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val value:Boolean?=snapshot.value as Boolean?
                    if (value!=null && value) {
                        holder.txtLastMessage.typeface = Typeface.DEFAULT
                        holder.imgShowUnseenMsg.visibility=View.GONE
                    } else {
                        holder.txtLastMessage.typeface = Typeface.DEFAULT_BOLD
                        holder.imgShowUnseenMsg.visibility=View.VISIBLE
                    }
                }
            })

        rlGroupFragment.visibility=View.GONE
        progressBarGroupFragment.visibility=View.GONE

        holder.llGroupLayout.setOnClickListener {
            val intent=Intent(context,ChatActivity::class.java)
            intent.putExtra("name",group.name)
            intent.putExtra("groupName",group.randomName+group.firstName)
            intent.putExtra("image",group.image)
            intent.putExtra("postKey",postKey)
            context.startActivity(intent)
        }

        holder.imgGroupIcon.setOnClickListener {
            val builder= AlertDialog.Builder(context)
            val postImage= RoundedImageView(context)
            postImage.minimumHeight=600
            postImage.cornerRadius=5F
            postImage.setPaddingRelative(5,5,5,5)
            Picasso.with(context).load(group.image).placeholder(R.drawable.profile).into(postImage)
            builder.setView(postImage)
            builder.create()
            builder.show()
        }
    }
}