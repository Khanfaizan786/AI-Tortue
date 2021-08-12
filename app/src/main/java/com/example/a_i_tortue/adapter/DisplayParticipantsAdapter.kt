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
import androidx.recyclerview.widget.RecyclerView
import com.example.a_i_tortue.R
import com.example.a_i_tortue.activity.MainActivity
import com.example.a_i_tortue.activity.SingleChatActivity
import com.example.a_i_tortue.model.Users
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class DisplayParticipantsAdapter(
    private val context:Context,
    private val participantsList:ArrayList<Users>,
    private val admin:String?,
    private val currentUserId:String,
    private val groupKey:String?,
    private val participantsList2:ArrayList<String>?
)
    :RecyclerView.Adapter<DisplayParticipantsAdapter.DisplayParticipantsViewHolder>() {
    class DisplayParticipantsViewHolder(view:View):RecyclerView.ViewHolder(view) {
        val imgParticipantIcon:CircleImageView=view.findViewById(R.id.imgParticipantIcon)
        val txtParticipantName:TextView=view.findViewById(R.id.txtParticipantName)
        val llParticipantLayout:LinearLayout=view.findViewById(R.id.llParticipantLayout)
        val txtStaticAdmin: TextView =view.findViewById(R.id.txtStaticAdmin)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DisplayParticipantsViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.all_participants_layout, parent,false)
        return DisplayParticipantsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return participantsList.size
    }

    override fun onBindViewHolder(holder: DisplayParticipantsViewHolder, position: Int) {
        val participant=participantsList[position]
        holder.txtStaticAdmin.visibility=View.GONE
        if (participant.uid==currentUserId) {
            val name="You"
            holder.txtParticipantName.text = name
        } else {
            holder.txtParticipantName.text=participant.name
        }

        if (participant.uid==admin) {
            holder.txtStaticAdmin.visibility=View.VISIBLE
        }
        if (participant.profileImage!=null) {
            try {
                Picasso.with(context).load(Uri.parse(participant.profileImage)).placeholder(R.drawable.profile)
                    .into(holder.imgParticipantIcon)
            } catch (e: Exception) {
            }
        }
        holder.llParticipantLayout.setOnClickListener {
            if (currentUserId==admin && participant.uid!=currentUserId) {
                val options = arrayOf("Message ${participant.name}","View ${participant.name}", "Remove ${participant.name}")

                val builder=AlertDialog.Builder(context)
                builder.setItems(options) { _, which ->
                    when (which) {
                        0-> {
                            val intent= Intent(context, SingleChatActivity::class.java)
                            intent.putExtra("name",participant.name)
                            intent.putExtra("uid",participant.uid)
                            var image:String?="none"
                            if (participant.profileImage!=null) {
                                image=participant.profileImage
                            }
                            intent.putExtra("image",image)
                            intent.putExtra("from","GroupDisplay")
                            context.startActivity(intent)
                        }

                        1-> {
                            val intent=Intent(context, MainActivity::class.java)
                            intent.putExtra("from","SingleChatActivity")
                            intent.putExtra("userId",participant.uid)
                            intent.putExtra("postKey","none")
                            context.startActivity(intent)
                        }

                        2-> {
                            val builder2=AlertDialog.Builder(context)
                            builder2.setMessage("Remove ${participant.name} from group")
                            builder2.setPositiveButton("Remove") { _,_->
                                if (participant.uid!=null) {
                                    removeParticipant(participant.uid,participant)
                                    notifyDataSetChanged()
                                }
                            }
                            builder2.setNegativeButton("Cancel") { _,_-> }
                            builder2.create().show()
                        }
                    }
                }
                builder.create().show()
            } else if(currentUserId!=admin && participant.uid!=currentUserId) {
                val options = arrayOf("Message ${participant.name}","View ${participant.name}")

                val builder=AlertDialog.Builder(context)
                builder.setItems(options) { _, which ->
                    when (which) {
                        0-> {
                            val intent= Intent(context, SingleChatActivity::class.java)
                            intent.putExtra("name",participant.name)
                            intent.putExtra("uid",participant.uid)
                            var image:String?="none"
                            if (participant.profileImage!=null) {
                                image=participant.profileImage
                            }
                            intent.putExtra("image",image)
                            intent.putExtra("from","GroupDisplay")
                            context.startActivity(intent)
                        }

                        1-> {
                            val intent=Intent(context, MainActivity::class.java)
                            intent.putExtra("from","SingleChatActivity")
                            intent.putExtra("userId",participant.uid)
                            intent.putExtra("postKey","none")
                            context.startActivity(intent)
                        }
                    }
                }
                builder.create().show()
            }
        }
    }

    private fun removeParticipant(
        uid: String?,
        participant: Users
    ) {
        val groupUsersRef:DatabaseReference=FirebaseDatabase.getInstance().reference.child("Group Users").child(groupKey!!)
        val userGroups:DatabaseReference=FirebaseDatabase.getInstance().reference.child("User Groups").child(uid!!)
        groupUsersRef.child(uid).removeValue()
        userGroups.child(groupKey).removeValue()
        participantsList.remove(participant)
        participantsList2?.remove(uid)
        notifyDataSetChanged()
    }
}