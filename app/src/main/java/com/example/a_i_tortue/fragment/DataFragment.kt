package com.example.a_i_tortue.fragment

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a_i_tortue.adapter.DatasetAdapter
import com.example.a_i_tortue.R
import com.example.a_i_tortue.model.Dataset
import com.google.firebase.database.*

/**
 * A simple [Fragment] subclass.
 */
class DataFragment : Fragment() {

    private lateinit var layoutManager: RecyclerView.LayoutManager

    private lateinit var dataRef:DatabaseReference
    var dataList= arrayListOf<Dataset>()
    private lateinit var datasetAdapter: DatasetAdapter
    private lateinit var recyclerData:RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view=inflater.inflate(R.layout.fragment_data, container, false)

        dataRef=FirebaseDatabase.getInstance().reference.child("Data")

        recyclerData=view.findViewById(R.id.recyclerData)
        layoutManager= LinearLayoutManager(activity)

        getAllDatasets()

        return view
    }

    private fun startDownload(uri: String) {
        val request=DownloadManager.Request(Uri.parse(uri))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle("Download")
        request.setDescription("Downloading file")
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"${System.currentTimeMillis()}")
        val manager:DownloadManager= activity?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }

    private fun getAllDatasets() {

        dataRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (ds in dataSnapshot.children) {
                        val dataset: Dataset? = ds.getValue(Dataset::class.java)
                        if (dataset != null) {
                        dataList.add(dataset)
                        }
                    }
                    if (activity!=null) {
                        datasetAdapter =
                            DatasetAdapter(
                                activity as Context,
                                activity as Activity,
                                dataList
                            )
                    }

                    recyclerData.adapter = datasetAdapter
                    recyclerData.layoutManager=layoutManager
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                val message=databaseError.message
                if (activity!=null) {
                    Toast.makeText(activity,"Error occured: $message",Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}