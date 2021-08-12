package com.example.a_i_tortue.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a_i_tortue.adapter.DocumentsAdapter

import com.example.a_i_tortue.R
import com.example.a_i_tortue.model.Document
import com.google.firebase.database.*

class DocumentFragment : Fragment() {

    private lateinit var recyclerDocument:RecyclerView
    private lateinit var documentRef:DatabaseReference
    var documentList= arrayListOf<Document>()
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var documentsAdapter: DocumentsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view=inflater.inflate(R.layout.fragment_document, container, false)

        documentRef=FirebaseDatabase.getInstance().reference.child("Documents")
        recyclerDocument=view.findViewById(R.id.recyclerDocument)
        layoutManager= GridLayoutManager(activity,2)

        getAllDocuments()

        return view
    }

    private fun getAllDocuments() {

        documentRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (ds in dataSnapshot.children) {
                        val dataset: Document? = ds.getValue(Document::class.java)
                        if (dataset != null) {
                            documentList.add(dataset)
                        }
                    }
                    if (activity!=null) {
                        documentsAdapter =
                            DocumentsAdapter(
                                activity as Context,
                                documentList
                            )
                    }

                    recyclerDocument.adapter = documentsAdapter
                    recyclerDocument.layoutManager=layoutManager
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                val message=databaseError.message
                if (activity!=null) {
                    Toast.makeText(activity,"Error occured: $message", Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}