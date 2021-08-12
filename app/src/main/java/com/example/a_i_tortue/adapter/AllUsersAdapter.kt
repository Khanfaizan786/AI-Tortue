package com.example.a_i_tortue.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.a_i_tortue.R
import com.example.a_i_tortue.activity.SingleChatActivity
import com.example.a_i_tortue.model.Users
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class AllUsersAdapter(private val context:Context,private val allUsersList:ArrayList<Users>):RecyclerView.Adapter<AllUsersAdapter.AllUsersViewHolder>() {
    class AllUsersViewHolder(view: View):RecyclerView.ViewHolder(view){
        val imgUserProfileIcon: CircleImageView =view.findViewById(R.id.imgUserProfileIcon)
        val imgSelectMember: ImageView =view.findViewById(R.id.imgSelectMember)
        val txtCreateGroupUserName: TextView =view.findViewById(R.id.txtCreateGroupUserName)
        val rlAllUsers: RelativeLayout =view.findViewById(R.id.rlAllUsers)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllUsersViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.all_users_layout,parent,false)
        return AllUsersViewHolder(view)
    }

    override fun getItemCount(): Int {
        return allUsersList.size
    }

    override fun onBindViewHolder(holder: AllUsersViewHolder, position: Int) {
        val user=allUsersList[position]
        holder.imgSelectMember.visibility=View.GONE
        holder.txtCreateGroupUserName.text=user.name
        if (user.profileImage!=null) {
            try {
                Picasso.with(context).load(Uri.parse(user.profileImage)).placeholder(R.drawable.profile)
                    .into(holder.imgUserProfileIcon)
            } catch (e: Exception) {
            }
        } else {
            Toast.makeText(context,"Profile Image is null",Toast.LENGTH_SHORT).show()
        }
        holder.rlAllUsers.setOnClickListener {
            val intent=Intent(context,SingleChatActivity::class.java)
            intent.putExtra("uid",user.uid)
            intent.putExtra("name",user.name)
            if (user.profileImage!=null) {
                intent.putExtra("image",user.profileImage)
            } else {
                intent.putExtra("image","none")
            }
            intent.putExtra("postKey","none")
            intent.putExtra("from","AllUsersFragment")
            context.startActivity(intent)
        }
    }
}