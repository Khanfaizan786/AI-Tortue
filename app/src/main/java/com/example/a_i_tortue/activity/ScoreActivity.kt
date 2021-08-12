package com.example.a_i_tortue.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.view.ViewCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.a_i_tortue.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class ScoreActivity : AppCompatActivity() {

    private lateinit var mAuth:FirebaseAuth
    private lateinit var nameRef:DatabaseReference
    private lateinit var txtScore:TextView
    private lateinit var txtPopupScore:TextView
    private lateinit var txtCorrect:TextView
    private lateinit var txtIncorrect:TextView
    private lateinit var txtScoreCardName:TextView
    private lateinit var btnClose:Button
    private lateinit var popupBackground:LinearLayout
    private lateinit var llPopupScore:LinearLayout
    private lateinit var imgScoreLike:CircleImageView
    private lateinit var fromsmall:Animation
    private lateinit var fromnothing:Animation
    private lateinit var forlike:Animation
    private lateinit var togo:Animation
    private lateinit var scoreProgressBar:ProgressBar
    private lateinit var currentUserId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        mAuth=FirebaseAuth.getInstance()
        currentUserId= mAuth.currentUser?.uid.toString()
        nameRef =FirebaseDatabase.getInstance().reference.child("Users").child(currentUserId)

        txtScore=findViewById(R.id.txtScore)
        txtPopupScore=findViewById(R.id.txtPopupScore)
        txtCorrect=findViewById(R.id.txtCorrect)
        txtIncorrect=findViewById(R.id.txtIncorrect)
        txtScoreCardName=findViewById(R.id.txtScoreCardName)
        btnClose=findViewById(R.id.btnScoreClose)
        popupBackground=findViewById(R.id.popupBackground)
        llPopupScore=findViewById(R.id.llPopupScore)
        imgScoreLike=findViewById(R.id.imgScoreLike)
        scoreProgressBar=findViewById(R.id.scoreProgressBar)

        fromsmall=AnimationUtils.loadAnimation(this@ScoreActivity,R.anim.fromsmall)
        fromnothing=AnimationUtils.loadAnimation(this@ScoreActivity,R.anim.fromnothing)
        forlike=AnimationUtils.loadAnimation(this@ScoreActivity,R.anim.forlike)
        togo=AnimationUtils.loadAnimation(this@ScoreActivity,R.anim.togo)

        val correct=intent.getIntExtra("score",0)
        val score=(correct*100)/15
        val percentScore="$score%"

        txtPopupScore.text=percentScore

        imgScoreLike.startAnimation(forlike)
        popupBackground.startAnimation(fromnothing)
        llPopupScore.startAnimation(fromsmall)

        popupBackground.alpha=1F

        val postListener=object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                val message=error.message
                Toast.makeText(this@ScoreActivity,"Error occured: $message",Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("name")) {
                        val name=snapshot.child("name").value.toString()
                        txtScoreCardName.text=name
                    }
                }
            }
        }

        nameRef.addValueEventListener(postListener)

        val incorrect=15-correct
        val correctS="Correct Answers- $correct"
        val incorrectS="Incorrect Answers- $incorrect"

        txtScore.text=percentScore
        scoreProgressBar.progress=score
        txtCorrect.text=correctS
        txtIncorrect.text=incorrectS

        btnClose.setOnClickListener {
            popupBackground.alpha=0F
            llPopupScore.startAnimation(togo)
            imgScoreLike.startAnimation(togo)
            ViewCompat.animate(llPopupScore).setStartDelay(1000).alpha(0F).start()
            ViewCompat.animate(imgScoreLike).setStartDelay(1000).alpha(0F).start()
        }
    }

    override fun onBackPressed() {
        if (popupBackground.alpha==1F) {
            popupBackground.alpha=0F
            llPopupScore.startAnimation(togo)
            imgScoreLike.startAnimation(togo)
            ViewCompat.animate(llPopupScore).setStartDelay(1000).alpha(0F).start()
            ViewCompat.animate(imgScoreLike).setStartDelay(1000).alpha(0F).start()
        } else {
            super.onBackPressed()
        }
    }
}