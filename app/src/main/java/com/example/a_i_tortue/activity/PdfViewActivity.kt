package com.example.a_i_tortue.activity

import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.example.a_i_tortue.R
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.*
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class PdfViewActivity : AppCompatActivity() {

    private lateinit var toolbarPdf: Toolbar
    private lateinit var txtToolbarPdf:TextView
    lateinit var pdfViewer:PDFView
    private var pdfFile:String?=null
    private var pdfName:String?=null
    private lateinit var pdfReference: DatabaseReference
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_view)

        pdfReference=FirebaseDatabase.getInstance().reference.child("Documents")

        toolbarPdf=findViewById(R.id.toolbarPdf)
        setSupportActionBar(toolbarPdf)

        txtToolbarPdf=findViewById(R.id.txtToolbarPdf)
        pdfViewer=findViewById(R.id.pdfViewer)
        progressBar=findViewById(R.id.progressBarPdf)

        progressBar.visibility=View.VISIBLE

        if (intent!=null) {
            pdfName=intent.getStringExtra("name")
            pdfFile=intent.getStringExtra("file")
        }

        if (pdfName!=null) {
            txtToolbarPdf.text=pdfName
        }

        if (pdfFile!=null) {
            RetrievePdfStream().execute(pdfFile)
        }
    }

    inner class RetrievePdfStream : AsyncTask<String, Void, InputStream>() {
        override fun doInBackground(vararg params: String?): InputStream? {
            var inputStream: InputStream? =null
            try {
                val url:URL=URL(params[0])
                val urlConnection: HttpsURLConnection =url.openConnection() as HttpsURLConnection
                if (urlConnection.responseCode==200) {
                    inputStream=BufferedInputStream(urlConnection.inputStream)
                }
            } catch (e:IOException) {
                return null
            }
            return inputStream
        }

        override fun onPostExecute(result: InputStream?) {
            pdfViewer.fromStream(result).load()
            progressBar.visibility=View.GONE
        }
    }
}
