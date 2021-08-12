package com.example.a_i_tortue.activity

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import com.example.a_i_tortue.R
import com.example.a_i_tortue.adapter.TabsAdapter
import com.example.a_i_tortue.fragment.*
import com.google.android.material.tabs.TabLayout

class MessageActivity : AppCompatActivity() {

    private lateinit var chatToolbar:Toolbar
    private lateinit var chatTabLayout:TabLayout
    private lateinit var chatTabsPager:ViewPager
    private lateinit var tabsAdapter: TabsAdapter
    private lateinit var frame2:FrameLayout

    private var postKey:String?="none"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        chatToolbar=findViewById(R.id.toolbarChat)
        setSupportActionBar(chatToolbar)
        supportActionBar?.title = "Tortue Direct"
        frame2=findViewById(R.id.frame2)

        chatTabLayout=findViewById(R.id.chatTabLayout)
        chatTabsPager=findViewById(R.id.chatTabsPager)
        tabsAdapter=TabsAdapter(supportFragmentManager)
        chatTabsPager.adapter=tabsAdapter
        chatTabLayout.setupWithViewPager(chatTabsPager)

        if (intent!=null) {
            val from=intent.getStringExtra("from")
            postKey=intent.getStringExtra("postKey")
            if (from=="GroupInfoActivity" || from=="CreateGroupFragment") {
                chatTabsPager.currentItem=1
            }
        }

        if (postKey!=null && postKey!="none") {
            supportActionBar?.title = "Send to.."
        }

    }

    override fun onBackPressed() {

        val fragment:Fragment?=supportFragmentManager.findFragmentByTag("android:switcher:${R.id.chatTabsPager}:${chatTabsPager.currentItem}")
        val frag:Fragment?=supportFragmentManager.findFragmentById(R.id.frame2)
        if (chatTabsPager.currentItem==0 && fragment!=null) {
            if (frag==null) {
                val mainIntent=Intent(this@MessageActivity,MainActivity::class.java)
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(mainIntent)
                finish()
            } else {
                val mainIntent=Intent(this@MessageActivity,MessageActivity::class.java)
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(mainIntent)
            }
        } else if (chatTabsPager.currentItem==1 && fragment!=null) {
            if (frag==null) {
                chatTabsPager.currentItem=0
            } else {
                val mainIntent=Intent(this@MessageActivity,MessageActivity::class.java)
                mainIntent.putExtra("from","CreateGroupFragment")
                mainIntent.putExtra("postKey","none")
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(mainIntent)
            }
        }
    }

    fun sendData():String {
        return if (postKey!=null) {
            postKey!!
        } else {
            "none"
        }
    }
}