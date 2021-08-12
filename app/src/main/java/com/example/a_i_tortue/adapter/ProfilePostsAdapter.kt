package com.example.a_i_tortue.adapter

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.a_i_tortue.R
import com.example.a_i_tortue.fragment.PostFragment
import com.example.a_i_tortue.model.Posts
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso

class ProfilePostsAdapter(val context: Context, private val profilePostList:ArrayList<Posts>): RecyclerView.Adapter<ProfilePostsAdapter.ProfilePostViewHolder>() {
    class ProfilePostViewHolder(view:View):RecyclerView.ViewHolder(view) {
        val imgProfilePost:ImageView=view.findViewById(R.id.imgProfilePost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePostViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.all_profile_posts_layout, parent,false)
        return ProfilePostViewHolder(view)
    }

    override fun getItemCount(): Int {
        return profilePostList.size
    }

    override fun onBindViewHolder(holder: ProfilePostViewHolder, position: Int) {
        val profilePost=profilePostList[position]

        if (profilePost.postImage!="none" && profilePost.postImage!=null) {
            try {
                Picasso.with(context).load(profilePost.postImage).into(holder.imgProfilePost)
            } catch (e:Exception) {
            }
        }

        holder.imgProfilePost.setOnLongClickListener {

            return@setOnLongClickListener true
        }

        holder.imgProfilePost.setOnLongClickListener {
            val builder=AlertDialog.Builder(context)
            val postImage= RoundedImageView(context)
            postImage.cornerRadius=5F
            postImage.setPaddingRelative(5,5,5,5)
            Picasso.with(context).load(profilePost.postImage).into(postImage)
            builder.setView(postImage)
            builder.create()
            builder.show()
            return@setOnLongClickListener true
        }

        holder.imgProfilePost.setOnClickListener {
            val bundle= Bundle()
            bundle.putString("postKey",profilePost.randomName+profilePost.uid)
            val postFragment=PostFragment()
            postFragment.arguments=bundle

            val transaction=(context as AppCompatActivity).supportFragmentManager.beginTransaction()
            transaction.addToBackStack("Profile Fragment")
            transaction.replace(R.id.frame,postFragment)
            transaction.commit()
        }
    }
}