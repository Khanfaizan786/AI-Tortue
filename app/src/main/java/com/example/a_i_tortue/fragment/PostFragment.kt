package com.example.a_i_tortue.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a_i_tortue.R
import com.example.a_i_tortue.activity.AddPostActivity
import com.example.a_i_tortue.activity.MainActivity
import com.example.a_i_tortue.activity.MessageActivity
import com.example.a_i_tortue.adapter.DatasetAdapter
import com.example.a_i_tortue.adapter.PostsAdapter
import com.example.a_i_tortue.model.Dataset
import com.example.a_i_tortue.model.Posts
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class PostFragment : Fragment() {

    private lateinit var postsRef:Query
    private lateinit var postKeyRef:DatabaseReference

    private lateinit var btnAddPost:FloatingActionButton
    private lateinit var recyclerPosts:RecyclerView
    var postList= arrayListOf<Posts>()
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var postAdapter:PostsAdapter
    private lateinit var postToolbar:Toolbar
    private var count=0
    private var position=1000
    private var postKey:String?="none"
    private var isScrolling:Boolean=false
    private var isLastItemReached:Boolean=false
    var lastItem=0
    private var lastVisible: String="postKey"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view=inflater.inflate(R.layout.fragment_post, container, false)

        postsRef = FirebaseDatabase.getInstance().reference.child("Posts").orderByKey().limitToLast(5)
        postKeyRef = FirebaseDatabase.getInstance().reference.child("Posts")

        btnAddPost=view.findViewById(R.id.btnAddPost)
        recyclerPosts=view.findViewById(R.id.recyclerPosts)
        postToolbar=view.findViewById(R.id.toolbarFeed)

        (activity as AppCompatActivity).setSupportActionBar(postToolbar)

        layoutManager = LinearLayoutManager(activity)

        if (activity!=null) {
            postAdapter = PostsAdapter(activity as Context, postList)
        }

        if(arguments!=null) {
            postKey=arguments!!.getString("postKey")
            if (postKey != "none" && postKey != null) {
                postKeyRef.child(postKey!!).addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {

                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        postList.clear()
                        count++
                        isLastItemReached=true
                        if (snapshot.exists()) {
                            val post:Posts?=snapshot.getValue(Posts::class.java)
                            if (post!=null) {
                                postList.add(post)
                            }
                            if (activity != null && postList.size!=0) {
                                postAdapter = PostsAdapter(activity as Context, postList)
                                recyclerPosts.adapter = postAdapter
                                recyclerPosts.layoutManager = layoutManager
                            }
                        }
                        if (activity != null && postList.size==0) {
                            Toast.makeText(activity,"Post has been deleted",Toast.LENGTH_SHORT).show()

                        }
                    }
                })
            }
        } else {
            getAllFeeds()
        }

        btnAddPost.setOnClickListener {
            val intent = Intent(activity, AddPostActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun getAllFeeds() {
        postsRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                val message=error.message
                if (activity!=null) {
                    Toast.makeText(activity,"Error occurred: $message", Toast.LENGTH_LONG).show()
                }
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChildren()) {
                    postList.clear()
                    for (ds in snapshot.children) {
                        val post: Posts? = ds.getValue(Posts::class.java)
                        if (post != null) {
                            postList.add(post)
                        }
                    }
                    postList.reverse()
                    if (activity != null && count == 0) {
                        postAdapter = PostsAdapter(activity as Context, postList)
                        recyclerPosts.adapter = postAdapter
                        recyclerPosts.layoutManager = layoutManager
                        lastVisible = postList[postAdapter.itemCount-1].randomName+postList[postAdapter.itemCount - 1].uid
                    }
                    if (activity!=null) {
                        Toast.makeText(activity,"First Item Loaded", Toast.LENGTH_LONG).show()
                    }
                    if (position != 1000) {
                        recyclerPosts.scrollToPosition(position)
                        btnAddPost.visibility = View.GONE
                    }
                    count++

                    val onScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener(){
                            override fun onScrollStateChanged(
                                recyclerView: RecyclerView,
                                newState: Int
                            ) {
                                super.onScrollStateChanged(recyclerView, newState)

                                if (newState==AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                                    isScrolling=true
                                }
                            }

                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)

                            val firstVisibleItem=layoutManager.findFirstVisibleItemPosition()
                            val visibleItemCount=layoutManager.childCount
                            val totalItemCount=layoutManager.itemCount

                            if (isScrolling && (firstVisibleItem + visibleItemCount == totalItemCount) && !isLastItemReached ) {
                                isScrolling=false

                                val nextQuery:Query= FirebaseDatabase.getInstance().reference.child("Posts").orderByKey().endAt(lastVisible).limitToLast(5)
                                var size=0
                                nextQuery.addValueEventListener(object : ValueEventListener{
                                    override fun onCancelled(error: DatabaseError) {

                                    }

                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val shortPostList= arrayListOf<Posts>()
                                        for (ds in snapshot.children) {
                                            val post: Posts? = ds.getValue(Posts::class.java)
                                            if (post != null) {
                                                shortPostList.add(post)
                                                size++
                                            }
                                        }
                                        shortPostList.reverse()
                                        shortPostList.removeAt(0)

                                        if (shortPostList.size>0) {
                                            if (postList[postList.size-4].randomName+postList[postList.size-4].uid != shortPostList[0].randomName+shortPostList[0].uid && activity!=null && lastItem<2 ) {
                                                postList.addAll(shortPostList)
                                                postAdapter.notifyDataSetChanged()
                                                Toast.makeText(activity,"${postList.size}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                        if (activity!=null) {
                                            lastVisible = postList[postAdapter.itemCount-1].randomName + postList[postAdapter.itemCount-1].uid
                                        }

                                        if (size<5) {
                                            isLastItemReached=true
                                            lastItem++
                                        }
                                    }
                                })
                            }
                        }
                    }
                    recyclerPosts.addOnScrollListener(onScrollListener)
                }
            }
        })
    }
}