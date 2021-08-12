package com.example.a_i_tortue.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.a_i_tortue.R
import com.example.a_i_tortue.activity.QuizActivity
import com.example.a_i_tortue.activity.ScoreActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class HomeFragment : Fragment() {

    lateinit var mAuth:FirebaseAuth
    var currentUser:FirebaseUser?=null
    lateinit var crdViewModuleTest:CardView
    lateinit var crdViewData:CardView
    lateinit var cardViewRating:CardView
    lateinit var crdViewDocument:CardView
    lateinit var crdViewBlogs:CardView
    lateinit var currentUserId:String
    lateinit var personRef:DatabaseReference
    lateinit var txtPersonDashboardName:TextView
    lateinit var imgPersonDashboard:CircleImageView
    lateinit var cardViewContact:CardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view=inflater.inflate(R.layout.fragment_home, container, false)

        mAuth=FirebaseAuth.getInstance()
        currentUser= mAuth.currentUser
        currentUserId= mAuth.currentUser?.uid.toString()
        personRef=FirebaseDatabase.getInstance().reference.child("Users").child(currentUserId)

        crdViewModuleTest =view.findViewById(R.id.cardViewModuleTests)
        crdViewData=view.findViewById(R.id.cardViewData)
        cardViewRating=view.findViewById(R.id.cardViewRating)
        crdViewDocument=view.findViewById(R.id.cardViewDocuments)
        crdViewBlogs=view.findViewById(R.id.cardViewBlogs)
        cardViewContact=view.findViewById(R.id.cardViewContact)
        txtPersonDashboardName=view.findViewById(R.id.txtPersonDashboardName)
        imgPersonDashboard=view.findViewById(R.id.imgPersonDashboard)

        personRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                val message=error.message
                if (activity!=null) {
                    Toast.makeText(activity,"Error occured: $message",Toast.LENGTH_LONG).show()
                }
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (currentUser!=null) {
                    if (snapshot.hasChild("profileImage")) {
                        val myProfileImage=snapshot.child("profileImage").value.toString()
                        Picasso.with(activity).load(myProfileImage).placeholder(R.drawable.profile).into(imgPersonDashboard)
                    }
                    if (snapshot.hasChild("name")) {
                        val myFullName:String = snapshot.child("name").value.toString()
                        txtPersonDashboardName.text=myFullName
                    }
                }
            }
        })

        crdViewModuleTest.setOnClickListener {
            val intent=Intent(activity, QuizActivity::class.java)
            startActivity(intent)
        }

        crdViewDocument.setOnClickListener {
            val fragment= DocumentFragment()
            val transaction=activity?.supportFragmentManager?.beginTransaction()
            transaction?.replace(R.id.frame,fragment)
            transaction?.commit()
        }

        cardViewContact.setOnClickListener {
            val fragment= ContactFragment()
            val transaction=activity?.supportFragmentManager?.beginTransaction()
            transaction?.replace(R.id.frame,fragment)
            transaction?.commit()
        }

        crdViewData.setOnClickListener {
            val fragment= DataFragment()
            val transaction=activity?.supportFragmentManager?.beginTransaction()
            transaction?.replace(R.id.frame,fragment)
            transaction?.commit()
        }

        cardViewRating.setOnClickListener {
            val fragment= RateFragment()
            val transaction=activity?.supportFragmentManager?.beginTransaction()
            transaction?.replace(R.id.frame,fragment)
            transaction?.commit()
        }

        crdViewBlogs.setOnClickListener {
            val fragment= DevelopersFragment()
            val transaction=activity?.supportFragmentManager?.beginTransaction()
            transaction?.replace(R.id.frame,fragment)
            transaction?.commit()
        }

        return view
    }

}
