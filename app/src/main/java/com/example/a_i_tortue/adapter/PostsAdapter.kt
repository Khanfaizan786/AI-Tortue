package com.example.a_i_tortue.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.a_i_tortue.R
import com.example.a_i_tortue.activity.CommentsActivity
import com.example.a_i_tortue.activity.MainActivity
import com.example.a_i_tortue.activity.MessageActivity
import com.example.a_i_tortue.model.Posts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PostsAdapter(val context:Context, private val postList:ArrayList<Posts>):RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    private var LikeChecker = false
    private val LikesRef: DatabaseReference= FirebaseDatabase.getInstance().reference.child("Likes");
    private val currentUserID: String? = FirebaseAuth.getInstance().currentUser?.uid

    inner class PostViewHolder(view:View):RecyclerView.ViewHolder(view) {

        var countLikes = 0

        val imgPostProfile:CircleImageView=view.findViewById(R.id.postProfileImage)
        val postUserName:TextView=view.findViewById(R.id.postUserName)
        val postDate:TextView=view.findViewById(R.id.postDate)
        val postTime:TextView=view.findViewById(R.id.postTime)
        val postImage:ImageView=view.findViewById(R.id.postImage)
        val postDescription:TextView=view.findViewById(R.id.postDescription)
        val postTitle:TextView=view.findViewById(R.id.postTitle)
        val imgLikeButton:ImageButton=view.findViewById(R.id.like_button)
        val displayNoOfLikes:TextView=view.findViewById(R.id.display_no_of_likes)
        val commentButton:ImageButton=view.findViewById(R.id.comment_button)
        val postLayout:LinearLayout=view.findViewById(R.id.post_layout)
        val profileLayout:LinearLayout=view.findViewById(R.id.profile_layout)
        val shareButton:ImageButton=view.findViewById(R.id.share_button)

        fun setLikeButtonStatus(PostKey: String?) {
            LikesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.child(PostKey!!).hasChild(currentUserID!!)) {
                        countLikes = dataSnapshot.child(PostKey).childrenCount.toInt()
                        imgLikeButton.setImageResource(R.drawable.like5)
                        displayNoOfLikes.text = "$countLikes Likes"
                    } else {
                        countLikes = dataSnapshot.child(PostKey).childrenCount.toInt()
                        imgLikeButton.setImageResource(R.drawable.like4)
                        displayNoOfLikes.text = "$countLikes Likes"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.all_feeds_layout, parent,false)
        return PostViewHolder(view)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post=postList[position]

        val PostKey= post.randomName+post.uid

        holder.postTitle.visibility=View.GONE
        holder.postDescription.visibility=View.GONE
        holder.postImage.visibility=View.GONE

        holder.postUserName.text=post.fullName
        holder.postDate.text=post.date
        holder.postTime.text=post.time
        if (post.title!="none") {
            holder.postTitle.visibility=View.VISIBLE
            holder.postTitle.text=post.title
        }
        if (post.description!="none") {
            holder.postDescription.visibility=View.VISIBLE
            holder.postDescription.text=post.description
        }


        holder.setLikeButtonStatus(PostKey)

        try {
            if (post.profileImage!=null) {

                Picasso.with(context).load(Uri.parse(post.profileImage)).placeholder(R.drawable.profile).into(holder.imgPostProfile)
            }
        } catch (e:Exception) {
            val message=e.message.toString()
            Toast.makeText(context,message,Toast.LENGTH_LONG).show()
        }
        if (post.postImage!="none") {
            holder.postImage.visibility=View.VISIBLE
            try {
                Picasso.with(context).load(Uri.parse(post.postImage)).placeholder(R.drawable.select_image)
                    .into(holder.postImage)
            } catch (e:Exception) {
                val message=e.message.toString()
                Toast.makeText(context,message,Toast.LENGTH_LONG).show()
            }
        }

        holder.imgLikeButton.setOnClickListener {
            LikeChecker = true
            LikesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (LikeChecker) {
                        LikeChecker = if (dataSnapshot.child(PostKey).hasChild(currentUserID!!)) {
                            LikesRef.child(PostKey).child(currentUserID).removeValue()
                            false
                        } else {
                            LikesRef.child(PostKey).child(currentUserID).setValue(true)
                            false
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

        holder.postLayout.setOnLongClickListener {
            if (post.uid==currentUserID) {
                val options = arrayOf("Edit Post", "Delete Post")
                val builder=AlertDialog.Builder(context)
                builder.setItems(options) { _,which ->
                    when(which) {
                        0 -> {
                            val options2 = arrayOf("Title","Description")
                            val builder4=AlertDialog.Builder(context)
                            builder4.setItems(options2) { _,which2 ->
                                when(which2) {
                                    0 -> {
                                        val builder2=AlertDialog.Builder(context)
                                        val inputField=EditText(context)
                                        inputField.setText(post.title)
                                        builder2.setTitle("Edit Title")
                                        builder2.setView(inputField)
                                        builder2.setPositiveButton("Save"){ _,_->
                                            val title=inputField.text.toString()
                                            val postRef=FirebaseDatabase.getInstance().reference.child("Posts").child(PostKey)
                                            postRef.child("title").setValue(title).addOnSuccessListener {
                                                holder.postTitle.text=title
                                                Toast.makeText(context,"Post updated successfully",Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        builder2.setNegativeButton("Cancel"){ _,_-> }
                                        builder2.create().show()
                                    }
                                    1 -> {
                                        val builder2=AlertDialog.Builder(context)
                                        val inputField=EditText(context)
                                        inputField.setText(post.description)
                                        builder2.setTitle("Edit Description")
                                        builder2.setView(inputField)
                                        builder2.setPositiveButton("Save"){ _,_->
                                            val description=inputField.text.toString()
                                            val postRef=FirebaseDatabase.getInstance().reference.child("Posts").child(PostKey)

                                            postRef.child("description").setValue(description).addOnSuccessListener {
                                                holder.postDescription.text=description
                                                Toast.makeText(context,"Post updated successfully",Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        builder2.setNegativeButton("Cancel"){ _,_-> }
                                        builder2.create().show()
                                    }
                                }
                            }
                            builder4.create().show()
                        }
                        1 -> {
                            val builder3=AlertDialog.Builder(context)
                            builder3.setMessage("Delete Post ?")
                            builder3.setPositiveButton("Delete") { _,_->
                                val postRef=FirebaseDatabase.getInstance().reference.child("Posts").child(PostKey)
                                postRef.removeValue().addOnSuccessListener {
                                    postList.remove(post)
                                    notifyDataSetChanged()
                                    Toast.makeText(context,"Post removed successfully",Toast.LENGTH_SHORT).show()
                                }
                            }
                            builder3.setNegativeButton("Cancel") { _,_-> }
                            builder3.create().show()
                        }
                    }
                }
                builder.create().show()
            }
            return@setOnLongClickListener true
        }

        holder.commentButton.setOnClickListener {
            val intent=Intent(context,CommentsActivity::class.java)
            intent.putExtra("postKey",post.randomName+post.uid)
            intent.putExtra("position","$position")
            context.startActivity(intent)
        }

        holder.shareButton.setOnClickListener {
            val intent=Intent(context,MessageActivity::class.java)
            intent.putExtra("postKey",PostKey)
            intent.putExtra("from","MainActivity")
            context.startActivity(intent)
        }

        holder.profileLayout.setOnClickListener {
            val intent=Intent(context, MainActivity::class.java)
            intent.putExtra("from","SingleChatActivity")
            intent.putExtra("userId",post.uid)
            intent.putExtra("postKey","none")
            context.startActivity(intent)
        }
    }

    private fun deletePost(postKey: String, position: Int) {

    }
}