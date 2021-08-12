package com.example.a_i_tortue.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.a_i_tortue.R
import com.example.a_i_tortue.activity.GroupInfoActivity
import com.example.a_i_tortue.adapter.AllUsersAdapter
import com.example.a_i_tortue.adapter.UsersAdapter
import com.example.a_i_tortue.model.Users
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class AllUsersFragment : Fragment() {

    private lateinit var toolbarAllUsers: Toolbar
    private lateinit var searchAllUsers: EditText
    private lateinit var txtSearchAllUsers: TextView

    private lateinit var recyclerAllUsers: RecyclerView
    private lateinit var allUsersAdapter: AllUsersAdapter
    private var allUserList= arrayListOf<Users>()

    private lateinit var allUsersDatabaseRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view=inflater.inflate(R.layout.fragment_all_users, container, false)

        allUsersDatabaseRef = FirebaseDatabase.getInstance().reference.child("Users")

        setHasOptionsMenu(true)

        toolbarAllUsers = view.findViewById(R.id.toolbarAllUsers)
        (activity as AppCompatActivity).setSupportActionBar(toolbarAllUsers)
        (activity as AppCompatActivity).supportActionBar?.title = "All Users"
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerAllUsers = view.findViewById(R.id.recyclerAllUsers)
        recyclerAllUsers.setHasFixedSize(true)
        recyclerAllUsers.layoutManager = LinearLayoutManager(activity)

        searchAllUsers = view.findViewById(R.id.searchAllUsers)

        txtSearchAllUsers=view.findViewById(R.id.txtSearchAllUsers)

        searchAllUsers.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText: String = searchAllUsers.text.toString()
                searchUsers(searchText)
            }

        })

        searchAllUsers.setOnEditorActionListener(object: TextView.OnEditorActionListener{
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId== EditorInfo.IME_ACTION_SEARCH) {
                    val searchText: String = searchAllUsers.text.toString()
                    searchAllUsers.clearFocus()
                    val imm= (activity as AppCompatActivity).getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(searchAllUsers.windowToken,0)
                    searchUsers(searchText)
                    return true
                }
                return false
            }
        })

        getAllUsers()

        return view
    }

    private fun searchUsers(searchText: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        allUsersDatabaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                allUserList.clear()
                for (ds in dataSnapshot.children) {
                    if (dataSnapshot.exists()) {
                        val users:Users? = ds.getValue<Users>(Users::class.java)
                        if (!users?.uid.equals(firebaseUser!!.uid)) {
                            if (users?.name?.toLowerCase(Locale.ROOT)?.contains(searchText.toLowerCase(Locale.ROOT))!!) {
                                allUserList.add(users)
                            }
                        }
                    }
                }
                if (allUserList.size==0) {
                    txtSearchAllUsers.visibility=View.VISIBLE
                } else {
                    txtSearchAllUsers.visibility=View.GONE
                }
                if (activity!=null) {
                    allUsersAdapter = AllUsersAdapter(activity as Context, allUserList)
                    allUsersAdapter.notifyDataSetChanged()
                    recyclerAllUsers.adapter=allUsersAdapter
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun getAllUsers() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        allUsersDatabaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                allUserList.clear()
                for (ds in dataSnapshot.children) {
                    if (dataSnapshot.exists()) {
                        val users:Users? = ds.getValue<Users>(Users::class.java)
                        if (!users?.uid.equals(firebaseUser!!.uid)) {
                            if (users != null) {
                                allUserList.add(users)
                            }
                        }
                    }
                }
                if (activity!=null) {
                    allUsersAdapter = AllUsersAdapter(activity as Context, allUserList)
                    recyclerAllUsers.adapter=allUsersAdapter
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
