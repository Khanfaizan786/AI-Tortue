package com.example.a_i_tortue.activity

import android.animation.Animator
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.a_i_tortue.R
import com.example.a_i_tortue.model.ModelQuiz
import com.google.android.material.floatingactionbutton.FloatingActionButton
class QuizActivity : AppCompatActivity() {

    private lateinit var txtQuestion:TextView
    private lateinit var txtTimer:TextView
    private lateinit var txtCount:TextView
    private lateinit var llAns:LinearLayout
    private lateinit var btnAnsSubmit:Button
    private lateinit var btnAns1:Button
    private lateinit var btnAns2:Button
    private lateinit var btnAns3:Button
    private lateinit var btnAns4:Button
    private var count = 0
    private var position = 0
    private var score = 0
    private var selectedOption:String? = null
    private val list=ArrayList<ModelQuiz>()
    private lateinit var countDownTimer:CountDownTimer

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        txtQuestion=findViewById(R.id.txtQuestion)
        txtCount=findViewById(R.id.txtCount)
        txtTimer=findViewById(R.id.txtTimer)
        llAns=findViewById(R.id.llAns)
        btnAnsSubmit=findViewById(R.id.btnAnsSubmit)
        btnAns1=findViewById(R.id.btnAns1)
        btnAns2=findViewById(R.id.btnAns2)
        btnAns3=findViewById(R.id.btnAns3)
        btnAns4=findViewById(R.id.btnAns4)

        countDownTimer= object : CountDownTimer(60000,1000) {
            override fun onFinish() {
                sendUserToScoreActivity()
            }

            override fun onTick(millisUntilFinished: Long) {
                var min="${millisUntilFinished/60000}"
                var sec="${(millisUntilFinished%60000)/1000}"

                if (min.length==1) {
                    min="0$min"
                }
                if (sec.length==1) {
                    sec="0$sec"
                }
                val time="$min:$sec"
                txtTimer.text=time
            }
        }
        countDownTimer.start()

        list.add(ModelQuiz("How many type of machine learning?","2","4","3","1","3"))
        list.add(ModelQuiz("In supervised learning labels are not present","True","False","none","none","False"))
        list.add(ModelQuiz("Select a right code to read a .csv file?","pd.read()","pd.read_excel()","pd.read_csv()","ps.read_xlsxl()","pd.read_csv()"))
        list.add(ModelQuiz("Select the categorical variable?","income","height","gender","price","gender"))
        list.add(ModelQuiz("How to identify the variable? Select right function.","df.columns()","df.columns","df.column","df.variable","df.columns"))
        list.add(ModelQuiz("What type of learning involves in heart disease prediction?","Supervised","Unsupervised","Reinforcement","Semi supervised","Supervised"))
        list.add(ModelQuiz("Select right statement.","ML is data based prediction technology","Data is not important in ML","Data is not more important","none","ML is data based prediction technology"))
        list.add(ModelQuiz("How to show last 3 columns and rows in dataset? Select the right code.","Df.head(3)","Df.tail()","Df.tail(3)","Df.tail()","Df.tail(3)"))
        list.add(ModelQuiz("How to show top 2 columns and rows in dataset? Select right code.","Df.head","Df.head(2)","Df.tail()","Df.tail","Df.head(2)"))
        list.add(ModelQuiz("What is bivariate analysis?","Analysis of one variable","Analysis of more than one variable","Analysis of two variables","Analysis of all variables","Analysis of two variables"))
        list.add(ModelQuiz("What is Univariate analysis?","Analysis of one variable","Analysis of more than one variable","Analysis of two variables","Analysis of all variables","Analysis of one variable"))
        list.add(ModelQuiz("House price prediction is an example of","Supervised learning","Unsupervised learning","Reinforcement learning","Semi supervised learning","Supervised learning"))
        list.add(ModelQuiz("Data type identification is not important in machine learning.","True","False","none","none","False"))
        list.add(ModelQuiz("Hypothesis generation done after the collection of the data.","True","False","none","none","False"))
        list.add(ModelQuiz("How many step of machine learning life cycle","1","2","5","6","6"))

        for (i in 0 until 4) {
            llAns.getChildAt(i).setOnClickListener {
                btnAns1.backgroundTintList=getColorStateList(R.color.colorDisableButton)
                btnAns2.backgroundTintList=getColorStateList(R.color.colorDisableButton)
                btnAns3.backgroundTintList=getColorStateList(R.color.colorDisableButton)
                btnAns4.backgroundTintList=getColorStateList(R.color.colorDisableButton)
                (it as Button).backgroundTintList = getColorStateList(R.color.colorButton)
                selectedOption= it.text.toString()
                btnAnsSubmit.isEnabled=true
                btnAnsSubmit.alpha= 1F
            }
        }
        playAnim(txtQuestion,0,list[position].question)

        btnAnsSubmit.setOnClickListener {
            checkSelectedOption(selectedOption,position)
            btnAnsSubmit.isEnabled=false
            btnAnsSubmit.alpha= 0.7F
            btnAns3.visibility=View.VISIBLE
            btnAns4.visibility=View.VISIBLE
            btnAns1.backgroundTintList=getColorStateList(R.color.colorDisableButton)
            btnAns2.backgroundTintList=getColorStateList(R.color.colorDisableButton)
            btnAns3.backgroundTintList=getColorStateList(R.color.colorDisableButton)
            btnAns4.backgroundTintList=getColorStateList(R.color.colorDisableButton)
            position++
            if (position==list.size) {
                sendUserToScoreActivity()
            }else {
                count=0
                playAnim(txtQuestion,0, list[position].question)
            }
        }
    }

    private fun sendUserToScoreActivity() {
        val intent= Intent(this@QuizActivity,
            ScoreActivity::class.java)
        intent.putExtra("score",score)
        startActivity(intent)
        finish()
    }

    private fun checkSelectedOption(selectedOption: String?, position: Int) {
        if (selectedOption==list[position].correctANS) {
            score++
        }
        Toast.makeText(this@QuizActivity,"$score",Toast.LENGTH_SHORT).show()
    }

    private fun playAnim(view:View,value:Int,data:String) {
        val postListener=object:Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {

                if (value==0) {
                    try {
                        (view as TextView).text = data
                        val text="${position+1}/${list.size}"
                        txtCount.text=text
                    }catch (ex:ClassCastException) {
                        (view as Button).text = data
                    }
                    view.tag = data

                    playAnim(view,1,data)
                }
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {
                if (value==0 && count<4) {
                    var option: String="None"
                    when (count) {
                        0-> {
                            option= list[position].optionA
                        }
                        1-> {
                            option= list[position].optionB
                        }
                        2-> {
                            option= list[position].optionC
                            if (option=="none") {
                                btnAns3.visibility=View.GONE
                            }
                        }
                        3-> {
                            option= list[position].optionD
                            if (option=="none") {
                                btnAns4.visibility=View.GONE
                            }
                        }
                    }
                    playAnim(llAns.getChildAt(count),0,option)
                    count++
                }
            }
        }

        view.animate().alpha(value.toFloat()).scaleX(value.toFloat()).scaleY(value.toFloat()).setDuration(500).setStartDelay(100)
            .setInterpolator(DecelerateInterpolator()).setListener(postListener)
    }

    override fun onBackPressed() {
        val dialog= AlertDialog.Builder(this)
        dialog.setTitle("Exit Quiz?")
        dialog.setMessage("If you exit now, you will be not permitted to attempt quiz until the result will announced")
        dialog.setPositiveButton("Exit") { _,_ ->
            super.onBackPressed()
        }
        dialog.setNegativeButton("Cancel") { _,_-> }
        dialog.create().show()
    }
}
