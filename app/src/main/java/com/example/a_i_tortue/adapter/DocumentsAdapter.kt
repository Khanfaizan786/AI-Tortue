package com.example.a_i_tortue.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.a_i_tortue.R
import com.example.a_i_tortue.activity.PdfViewActivity
import com.example.a_i_tortue.model.Document

class DocumentsAdapter (val context:Context, val documentsList:ArrayList<Document>):RecyclerView.Adapter<DocumentsAdapter.DocumentHolder>() {
    class DocumentHolder(view:View):RecyclerView.ViewHolder(view) {
        val txtDocumentName:TextView=view.findViewById(R.id.txtDocumentName)
        val cardViewLayout:CardView=view.findViewById(R.id.cardViewAllDocuments)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.all_documents_layout, parent,false)
        return DocumentHolder(view)
    }

    override fun getItemCount(): Int {
        return documentsList.size
    }

    override fun onBindViewHolder(holder: DocumentHolder, position: Int) {
        val document=documentsList[position]
        holder.txtDocumentName.text=document.name

        holder.cardViewLayout.setOnClickListener {
            val intent= Intent(context,PdfViewActivity::class.java)
            intent.putExtra("name",document.name)
            intent.putExtra("file",document.document)
            context.startActivity(intent)
        }
    }
}