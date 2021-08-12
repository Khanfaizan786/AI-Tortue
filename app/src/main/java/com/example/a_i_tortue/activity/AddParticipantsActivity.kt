package com.example.a_i_tortue.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a_i_tortue.R
import com.example.a_i_tortue.adapter.UsersAdapter
import com.example.a_i_tortue.model.Users
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_add_participants.*
import java.util.*

class AddParticipantsActivity : AppCompatActivity() {

    private lateinit var toolbarAddParticipants: Toolbar
    private lateinit var searchBoxAddParticipants: EditText
    private lateinit var llShowSelectedMembers2: LinearLayout
    private lateinit var txtNoOfSelectedMembers2: TextView
    private lateinit var txtStaticSearch2: TextView
    private lateinit var btnAddParticipantsFinal: FloatingActionButton
    private lateinit var progressBarAddParticipants:ProgressBar

    private lateinit var recyclerAllUsersList: RecyclerView
    private lateinit var usersAdapter: UsersAdapter
    private var userList= arrayListOf<Users>()
    private var selectedUsers= arrayListOf<String>()

    private lateinit var allUsersDatabaseRef: DatabaseReference
    private var groupKey:String?="GroupKey"
    private var membersList: ArrayList<String>?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_participants)

        allUsersDatabaseRef = FirebaseDatabase.getInstance().reference.child("Users")

        toolbarAddParticipants = findViewById(R.id.toolbarAddParticipants)
        setSupportActionBar(toolbarAddParticipants)
        supportActionBar?.setTitle("Select Participants")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerAllUsersList = findViewById(R.id.recyclerAllUsersList)
        recyclerAllUsersList.setHasFixedSize(true)
        recyclerAllUsersList.layoutManager = LinearLayoutManager(this@AddParticipantsActivity)

        searchBoxAddParticipants = findViewById(R.id.searchBoxAddParticipants)

        llShowSelectedMembers2=findViewById(R.id.llShowSelectedMembers2)
        txtNoOfSelectedMembers2=findViewById(R.id.txtNoOfSelectedMembers2)
        btnAddParticipantsFinal=findViewById(R.id.btnAddParticipantsFinal)
        txtStaticSearch2=findViewById(R.id.txtStaticSearch2)
        progressBarAddParticipants=findViewById(R.id.progressBarAddParticipants)

        if (intent!=null) {
            groupKey=intent.getStringExtra("groupKey")
            membersList=intent.getStringArrayListExtra("membersList")
        }

        searchBoxAddParticipants.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText: String = searchBoxAddParticipants.text.toString()
                searchUsers(searchText)
            }

        })

        searchBoxAddParticipants.setOnEditorActionListener(object: TextView.OnEditorActionListener{
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId== EditorInfo.IME_ACTION_SEARCH) {
                    val searchText: String = searchBoxAddParticipants.text.toString()
                    searchBoxAddParticipants.clearFocus()
                    val imm= getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(searchBoxAddParticipants.windowToken,0)
                    searchUsers(searchText)
                    return true
                }
                return false
            }
        })

        getAllUsers()

        btnAddParticipantsFinal.setOnClickListener {
            if (selectedUsers.size<=0) {
                Toast.makeText(this@AddParticipantsActivity,"Please choose at least 1 participant to proceed", Toast.LENGTH_LONG).show()
            } else if (groupKey=="GroupKey"){
                Toast.makeText(this@AddParticipantsActivity,"Please try again later", Toast.LENGTH_LONG).show()
            } else {
                progressBarAddParticipants.visibility=View.VISIBLE
                addParticipants()
            }
        }
    }

    private fun addParticipants() {
        val userGroups:DatabaseReference=FirebaseDatabase.getInstance().reference.child("User Groups")
        val groupUsers:DatabaseReference=FirebaseDatabase.getInstance().reference.child("Group Users")

        for (user in selectedUsers) {
            userGroups.child(user).child(groupKey!!).setValue(true)
            groupUsers.child(groupKey!!).child(user).setValue(true)
        }

        progressBarAddParticipants.visibility=View.GONE
        Toast.makeText(this@AddParticipantsActivity,"Participants added",Toast.LENGTH_SHORT).show()
        sendUserToMessageActivity()
    }

    private fun sendUserToMessageActivity() {
        val intent=Intent(this@AddParticipantsActivity,MessageActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("from","GroupInfoActivity")
        startActivity(intent)
        finish()
    }

    private fun searchUsers(searchText: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        allUsersDatabaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList.clear()
                for (ds in dataSnapshot.children) {
                    if (dataSnapshot.exists()) {
                        val users:Users? =
                            ds.getValue<Users>(Users::class.java)
                        if (!users?.uid.equals(firebaseUser!!.uid) && !membersList?.contains(users?.uid)!!) {
                            if (users?.name?.toLowerCase(Locale.ROOT)?.contains(searchText.toLowerCase(
                                    Locale.ROOT))!!) {
                                userList.add(users)
                            }
                        }
                    }
                }
                if (userList.size==0) {
                    txtStaticSearch2.visibility= View.VISIBLE
                } else {
                    txtStaticSearch2.visibility= View.GONE
                }
                usersAdapter = UsersAdapter(this@AddParticipantsActivity, userList,selectedUsers,llShowSelectedMembers2,txtNoOfSelectedMembers2)
                usersAdapter.notifyDataSetChanged()
                recyclerAllUsersList.adapter=usersAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun getAllUsers() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        allUsersDatabaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList.clear()
                for (ds in dataSnapshot.children) {
                    if (dataSnapshot.exists()) {
                        val users:Users? =
                            ds.getValue<Users>(Users::class.java)
                        if (!users?.uid.equals(firebaseUser!!.uid) && !membersList?.contains(users?.uid)!!) {
                            if (users != null) {
                                userList.add(users)
                            }
                        }
                    }
                }
                usersAdapter = UsersAdapter(this@AddParticipantsActivity, userList,selectedUsers,llShowSelectedMembers2,txtNoOfSelectedMembers2)
                recyclerAllUsersList.adapter=usersAdapter
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}
