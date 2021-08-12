package com.example.a_i_tortue.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.a_i_tortue.fragment.ChatFragment
import com.example.a_i_tortue.fragment.GroupFragment

class TabsAdapter(fm:FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> {
                ChatFragment()
            }
            1 -> {
                GroupFragment()
            }
            else -> {
                Fragment()
            }
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position) {
            0 -> "Chats"
            1 -> "Groups"
            else -> "None"
        }
    }
}