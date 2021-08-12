package com.example.a_i_tortue.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.example.a_i_tortue.R


class RateFragment : Fragment() {

    private lateinit var imgRate1:ImageView
    private lateinit var imgRate2:ImageView
    private lateinit var imgRate3:ImageView
    private lateinit var imgRate4:ImageView
    private lateinit var imgRate5:ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_rate, container, false)

        imgRate1=view.findViewById(R.id.imgRate1)
        imgRate2=view.findViewById(R.id.imgRate2)
        imgRate3=view.findViewById(R.id.imgRate3)
        imgRate4=view.findViewById(R.id.imgRate4)
        imgRate5=view.findViewById(R.id.imgRate5)

        imgRate1.setOnClickListener {
            imgRate1.setBackgroundResource(R.drawable.rate_color_logo)
        }

        imgRate2.setOnClickListener {
            imgRate1.setBackgroundResource(R.drawable.rate_color_logo)
            imgRate2.setBackgroundResource(R.drawable.rate_color_logo)
        }

        imgRate3.setOnClickListener {
            imgRate1.setBackgroundResource(R.drawable.rate_color_logo)
            imgRate2.setBackgroundResource(R.drawable.rate_color_logo)
            imgRate3.setBackgroundResource(R.drawable.rate_color_logo)
        }

        imgRate4.setOnClickListener {
            imgRate1.setBackgroundResource(R.drawable.rate_color_logo)
            imgRate2.setBackgroundResource(R.drawable.rate_color_logo)
            imgRate3.setBackgroundResource(R.drawable.rate_color_logo)
            imgRate4.setBackgroundResource(R.drawable.rate_color_logo)
        }

        imgRate5.setOnClickListener {
            imgRate1.setBackgroundResource(R.drawable.rate_color_logo)
            imgRate2.setBackgroundResource(R.drawable.rate_color_logo)
            imgRate3.setBackgroundResource(R.drawable.rate_color_logo)
            imgRate4.setBackgroundResource(R.drawable.rate_color_logo)
            imgRate5.setBackgroundResource(R.drawable.rate_color_logo)
        }
        return view
    }
}