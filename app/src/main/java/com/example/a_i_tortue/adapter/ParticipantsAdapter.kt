package com.example.a_i_tortue.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.a_i_tortue.R
import com.example.a_i_tortue.model.Users
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ParticipantsAdapter(val context:Context,val participantList:ArrayList<Users>):RecyclerView.Adapter<ParticipantsAdapter.ParticipantsViewHolder>() {
    class ParticipantsViewHolder(view: View): RecyclerView.ViewHolder(view){
        val imgParticipantIcon: CircleImageView =view.findViewById(R.id.imgParticipantIcon)
        val txtParticipantName: TextView =view.findViewById(R.id.txtParticipantName)
        val txtStaticAdmin: TextView =view.findViewById(R.id.txtStaticAdmin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantsViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.all_participants_layout,parent,false)
        return ParticipantsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return participantList.size
    }

    override fun onBindViewHolder(holder: ParticipantsViewHolder, position: Int) {
        val participant=participantList[position]
        holder.txtStaticAdmin.visibility=View.GONE
        holder.txtParticipantName.text=participant.name

        if (participant.profileImage!=null) {
            try {
                Picasso.with(context).load(Uri.parse(participant.profileImage)).placeholder(R.drawable.profile)
                    .into(holder.imgParticipantIcon)
            } catch (e: Exception) {
            }
        }
    }
}