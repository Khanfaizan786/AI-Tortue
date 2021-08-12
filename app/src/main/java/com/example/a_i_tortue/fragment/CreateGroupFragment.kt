package com.example.a_i_tortue.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a_i_tortue.activity.GroupInfoActivity
import com.example.a_i_tortue.R
import com.example.a_i_tortue.adapter.UsersAdapter
import com.example.a_i_tortue.model.Users
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class CreateGroupFragment : Fragment() {

    private lateinit var createGroupToolbar: Toolbar
    private lateinit var searchInputText: EditText
    private lateinit var llShowSelectedMembers:LinearLayout
    private lateinit var txtNoOfSelectedMembers:TextView
    private lateinit var txtNoItem:TextView
    private lateinit var btnCreateGroup:FloatingActionButton

    private lateinit var searchResultList: RecyclerView
    private lateinit var usersAdapter:UsersAdapter
    private var userList= arrayListOf<Users>()
    private var selectedUsers= arrayListOf<String>()

    private lateinit var allUsersDatabaseRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view=inflater.inflate(R.layout.fragment_create_group, container, false)

        allUsersDatabaseRef = FirebaseDatabase.getInstance().reference.child("Users")

        setHasOptionsMenu(true)

       createGroupToolbar = view.findViewById(R.id.toolbarCreateGroup)
        (activity as AppCompatActivity).setSupportActionBar(createGroupToolbar)
        (activity as AppCompatActivity).supportActionBar?.setTitle("Select Participants")
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        searchResultList = view.findViewById(R.id.search_result_list)
        searchResultList.setHasFixedSize(true)
        searchResultList.layoutManager = LinearLayoutManager(activity)

        searchInputText = view.findViewById(R.id.search_box_input)

        llShowSelectedMembers=view.findViewById(R.id.llShowSelectedMembers)
        txtNoOfSelectedMembers=view.findViewById(R.id.txtNoOfSelectedMembers)
        btnCreateGroup=view.findViewById(R.id.btnCreateGroup)
        txtNoItem=view.findViewById(R.id.txtStaticSearch)

        searchInputText.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText: String = searchInputText.text.toString()
                searchUsers(searchText)
            }

        })

        searchInputText.setOnEditorActionListener(object: TextView.OnEditorActionListener{
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId==EditorInfo.IME_ACTION_SEARCH) {
                    val searchText: String = searchInputText.text.toString()
                    searchInputText.clearFocus()
                    val imm= (activity as AppCompatActivity).getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(searchInputText.windowToken,0)
                    searchUsers(searchText)
                    return true
                }
                return false
            }
        })

        getAllUsers()

        btnCreateGroup.setOnClickListener {
            if (selectedUsers.size==0) {
                Toast.makeText(activity,"Please choose at least 1 participant to proceed",Toast.LENGTH_LONG).show()
            } else {
                val intent=Intent(activity, GroupInfoActivity::class.java)
                intent.putExtra("participants",selectedUsers)
                startActivity(intent)
            }
        }

        return view
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
                        if (!users?.uid.equals(firebaseUser!!.uid)) {
                            if (users?.name?.toLowerCase(Locale.ROOT)?.contains(searchText.toLowerCase(Locale.ROOT))!!) {
                                userList.add(users)
                            }
                        }
                    }
                }
                if (userList.size==0) {
                    txtNoItem.visibility=View.VISIBLE
                } else {
                    txtNoItem.visibility=View.GONE
                }
                if (activity!=null) {
                    usersAdapter = UsersAdapter(activity as Context, userList,selectedUsers,llShowSelectedMembers,txtNoOfSelectedMembers)
                    usersAdapter.notifyDataSetChanged()
                    searchResultList.adapter=usersAdapter
                }
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
                        if (!users?.uid.equals(firebaseUser!!.uid)) {
                            if (users != null) {
                                userList.add(users)
                            }
                        }
                    }
                }
                if (activity!=null) {
                    usersAdapter = UsersAdapter(activity as Context, userList,selectedUsers,llShowSelectedMembers,txtNoOfSelectedMembers)
                    searchResultList.adapter=usersAdapter
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }
}
