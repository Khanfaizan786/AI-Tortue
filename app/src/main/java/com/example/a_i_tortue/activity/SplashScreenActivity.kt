package com.example.a_i_tortue.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.airbnb.lottie.LottieAnimationView
import com.example.a_i_tortue.R

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var forlike: Animation
    private lateinit var imgSplashLogo:ImageView
    private lateinit var anim:LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash_screen)

        /*imgSplashLogo=findViewById(R.id.imgSplashLogo)
        forlike= AnimationUtils.loadAnimation(this@SplashScreenActivity,R.anim.forlike)

        imgSplashLogo.startAnimation(forlike)*/

        val handler = Handler()
        handler.postDelayed({
            startActivity(Intent(this@SplashScreenActivity, LoginActivity::class.java))
            finish()
        }, 5000)
    }


}
