package com.example.a_i_tortue.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.a_i_tortue.R
import com.example.a_i_tortue.activity.LoginActivity
import com.example.a_i_tortue.activity.MainActivity
import com.example.a_i_tortue.activity.MessageActivity
import com.example.a_i_tortue.adapter.GroupsAdapter
import com.example.a_i_tortue.model.Group
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class GroupFragment : Fragment() {

    private lateinit var groupRef:DatabaseReference
    private lateinit var groupUsersRef:DatabaseReference
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var rlGroupFragment: RelativeLayout
    private lateinit var progressBarGroupFragment: ProgressBar
    private var groupList= arrayListOf<Group>()
    private var userGroupList= arrayListOf<String>()
    private lateinit var groupAdapter: GroupsAdapter
    private lateinit var recyclerGroups:RecyclerView
    private lateinit var saveCurrentDate: String
    private lateinit var saveCurrentTime: String
    private lateinit var postRandomName: String
    private lateinit var usersRef:DatabaseReference
    private lateinit var currentUserId: String
    private lateinit var mAuth: FirebaseAuth
    private var postKey:String?="none"

    private var timeComparator=Comparator<Group>{group1,group2 ->
        group1.timeStamp.compareTo(group2.timeStamp,true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_group, container, false)

        setHasOptionsMenu(true)
        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth.currentUser?.uid.toString()
        groupRef=FirebaseDatabase.getInstance().reference.child("Groups")
        usersRef=FirebaseDatabase.getInstance().reference.child("User Groups")
        groupUsersRef=FirebaseDatabase.getInstance().reference.child("Group Users")

        recyclerGroups=view.findViewById(R.id.recyclerGroups)
        rlGroupFragment=view.findViewById(R.id.rlGroupFragment)
        progressBarGroupFragment=view.findViewById(R.id.progressBarGroupFragment)
        layoutManager= LinearLayoutManager(activity)

        rlGroupFragment.visibility=View.VISIBLE
        progressBarGroupFragment.visibility=View.VISIBLE

        if (activity!=null) {
            val messageActivity=activity as MessageActivity
            postKey=messageActivity.sendData()
        }

        getAllUserGroups()

        return view
    }

    private fun getAllUserGroups() {
        usersRef.child(currentUserId).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                val message=error.message
                if (activity!=null) {
                    Toast.makeText(activity,"Error occurred: $message",Toast.LENGTH_LONG).show()
                }
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                userGroupList.clear()
                if (snapshot.hasChildren()) {
                    for (ds in snapshot.children) {
                        userGroupList.add(ds.key.toString())
                    }
                    if (userGroupList.size!=0) {
                        getAllGroups()
                    }
                } else {
                    getAllGroups()
                    rlGroupFragment.visibility=View.GONE
                    progressBarGroupFragment.visibility=View.GONE
                }
            }
        })
    }

    private fun getAllGroups() {

        groupRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                groupList.clear()
                if (dataSnapshot.hasChildren()) {
                    for (ds in dataSnapshot.children) {
                        val group: Group? = ds.getValue(Group::class.java)
                        if (group != null) {
                            if (userGroupList.contains(group.randomName+group.firstName)) {
                                groupList.add(group)
                            }
                        }
                        Collections.sort(groupList,timeComparator)
                        groupList.reverse()
                        if (activity!=null) {
                            groupAdapter = GroupsAdapter(activity as Context,groupList,rlGroupFragment,progressBarGroupFragment,postKey!!)
                            groupAdapter.notifyDataSetChanged()
                            recyclerGroups.adapter = groupAdapter
                            recyclerGroups.layoutManager=layoutManager
                            //recyclerGroups.addItemDecoration(DividerItemDecoration(recyclerGroups.context,(layoutManager as LinearLayoutManager).orientation))
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                val message=databaseError.message
                if (activity!=null) {
                    Toast.makeText(activity,"Error occured: $message",Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        if (postKey=="none" || postKey==null) {
            inflater.inflate(R.menu.group_icon,menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId==R.id.action_create_group) {
            requestNewGroup()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun requestNewGroup() {
        /*val builder=AlertDialog.Builder(activity)
        builder.setTitle("Group name")

        val groupNameField=EditText(activity)
        groupNameField.hint = "e.g. AI Tortue"
        builder.setView(groupNameField)

        builder.setPositiveButton("Create") { _, _ ->
            val groupName=groupNameField.text.toString()
            if (TextUtils.isEmpty(groupName)) {
                Toast.makeText(activity,"Please enter group name",Toast.LENGTH_LONG).show()
            } else {
                createNewGroup(groupName)
            }
        }

        builder.setNegativeButton("Cancel") { _,_-> }
        builder.create()
        builder.show()*/

        val fragment= CreateGroupFragment()
        val transaction=activity?.supportFragmentManager?.beginTransaction()
        transaction?.replace(R.id.frame2,fragment)
        transaction?.addToBackStack("Group Fragment")?.commit()
    }

    private fun createNewGroup(groupName: String) {

        val callForDate = Calendar.getInstance()
        val currentDate = SimpleDateFormat("yyyy-MM-dd")
        saveCurrentDate = currentDate.format(callForDate.time)

        val callForTime = Calendar.getInstance()
        val currentTime = SimpleDateFormat("HH:mm:ss")
        saveCurrentTime = currentTime.format(callForTime.time)

        postRandomName = saveCurrentDate + saveCurrentTime

        val groupMap=HashMap<String,String>()
        groupMap["name"] = groupName
        groupMap["image"] = "none"
        groupMap["time"] = postRandomName
        groupRef.child(groupName).updateChildren(groupMap as Map<String, Any>).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(activity,"$groupName is created successfully",Toast.LENGTH_LONG).show()
            } else {
                val error=it.exception.toString()
                Toast.makeText(activity,"Error occurred: $error",Toast.LENGTH_LONG).show()
            }
        }
    }
}
