package com.example.a_i_tortue.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.a_i_tortue.R
import com.example.a_i_tortue.model.Comment
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentsAdapter(private val context: Context, private val postCommentsList: ArrayList<Comment>)
    : RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder>() {
    class CommentsViewHolder(view:View):RecyclerView.ViewHolder(view) {
        val commentsProfileImage:CircleImageView=view.findViewById(R.id.commentsProfileImage)
        val commentsFullname:TextView=view.findViewById(R.id.commentsFullname)
        val txtComment:TextView=view.findViewById(R.id.txtComment)
        val commentsDate:TextView=view.findViewById(R.id.commentsDate)
        val commentsTime:TextView=view.findViewById(R.id.commentsTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.all_comments_layout, parent,false)
        return CommentsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return postCommentsList.size
    }

    override fun onBindViewHolder(holder: CommentsViewHolder, position: Int) {
        val comment=postCommentsList[position]
        holder.txtComment.text=comment.comment
        holder.commentsFullname.text=comment.name
        holder.commentsDate.text=comment.date
        holder.commentsTime.text=comment.time

        if (comment.image!="none") {
            try {
                Picasso.with(context).load(Uri.parse(comment.image)).placeholder(R.drawable.profile)
                    .into(holder.commentsProfileImage)
            } catch (e: Exception) {
            }
        }
    }
}