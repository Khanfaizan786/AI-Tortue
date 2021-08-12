package com.example.a_i_tortue.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast

import com.example.a_i_tortue.R
import com.squareup.picasso.Picasso

class ClickPostFragment : Fragment() {

    private lateinit var imgClickPost:ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_click_post, container, false)

        imgClickPost=view.findViewById(R.id.imgClickPost)

        if (arguments!=null) {
            val postImage = arguments!!.getString("postImage").toString()
            if (activity!=null) {
                try {
                    Picasso.with(activity as Context).load(Uri.parse(postImage))
                        .into(imgClickPost)
                } catch (e:Exception) {
                    val message=e.message.toString()
                    Toast.makeText(activity,message, Toast.LENGTH_LONG).show()
                }
            }
        }

        return view
    }

}
