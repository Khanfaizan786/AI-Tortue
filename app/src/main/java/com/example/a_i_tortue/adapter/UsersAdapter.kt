package com.example.a_i_tortue.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.a_i_tortue.R
import com.example.a_i_tortue.model.Users
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UsersAdapter(
    val context: Context,
    private val userList:ArrayList<Users>,
    private var selectedUsers: ArrayList<String>,
    private var llShowSelectedMembers:LinearLayout,
    private var txtNoOfSelectedMembers:TextView
) : RecyclerView.Adapter<UsersAdapter.UsersViewHolder>(){

    class UsersViewHolder(view:View):RecyclerView.ViewHolder(view) {
        val imgUserProfileIcon: CircleImageView =view.findViewById(R.id.imgUserProfileIcon)
        val imgSelectMember: ImageView =view.findViewById(R.id.imgSelectMember)
        val txtCreateGroupUserName: TextView =view.findViewById(R.id.txtCreateGroupUserName)
        val rlAllUsers:RelativeLayout=view.findViewById(R.id.rlAllUsers)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.all_users_layout,parent,false)
        return UsersViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val user=userList[position]
        holder.txtCreateGroupUserName.text=user.name
        if (user.profileImage!=null) {
            try {
                Picasso.with(context).load(Uri.parse(user.profileImage)).placeholder(R.drawable.profile)
                    .into(holder.imgUserProfileIcon)
            } catch (e: Exception) {
            }
        }

        holder.imgSelectMember.visibility=View.GONE
        llShowSelectedMembers.visibility=View.GONE

        val userCount = "Participants selected : ${selectedUsers.size}"

        if (selectedUsers.contains(user.uid)) {
            holder.imgSelectMember.visibility=View.VISIBLE
            holder.imgSelectMember.setImageResource(R.drawable.ic_selected)
        }

        if (selectedUsers.size>0) {
            llShowSelectedMembers.visibility=View.VISIBLE
            txtNoOfSelectedMembers.text=userCount
        }

        holder.rlAllUsers.setOnClickListener{
            if (selectedUsers.contains(user.uid)) {
                selectedUsers.remove(user.uid)
                notifyDataSetChanged()
            } else {
                selectedUsers.add(user.uid!!)
                notifyDataSetChanged()
            }
        }
    }
}