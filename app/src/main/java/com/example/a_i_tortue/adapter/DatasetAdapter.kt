package com.example.a_i_tortue.adapter

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.a_i_tortue.R
import com.example.a_i_tortue.model.Dataset
import com.squareup.picasso.Picasso

class DatasetAdapter(val context:Context,val activity: Activity, val dataList:ArrayList<Dataset>): RecyclerView.Adapter<DatasetAdapter.DatasetHolder>() {

    private val STORAGE_REQUEST_CODE=100

    private val storagePermissions =
    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    class DatasetHolder(view: View): RecyclerView.ViewHolder(view) {
        val imgDataIcon:ImageView=view.findViewById(R.id.imgDatasetIcon)
        val imgDataDownload:ImageView=view.findViewById(R.id.imgDataDownload)
        val txtDatsetName:TextView=view.findViewById(R.id.txtDatasetName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DatasetHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.all_dataset_layout,parent,false)
        return DatasetHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: DatasetHolder, position: Int) {
        val dataset=dataList[position]
        holder.txtDatsetName.text=dataset.name
        try {
            Picasso.with(context).load(Uri.parse(dataset.image)).placeholder(R.mipmap.ic_launcher_round)
                .into(holder.imgDataIcon)
        } catch (e: Exception) {
        }
        holder.imgDataDownload.setOnClickListener {
            if (!checkStoragePermission()) {
                requestStoragePermission()
            } else {
                startDownload(dataset.file!!,dataset.name!!)
            }
        }
    }

    private fun checkStoragePermission(): Boolean {
        val result= ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val result1 = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        return result && result1
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            activity,
            storagePermissions,
            STORAGE_REQUEST_CODE
        )
    }

    private fun startDownload(uri: String, name: String) {
        val request= DownloadManager.Request(Uri.parse(uri))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle(name)
        request.setDescription("Downloading file")
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"$name ${System.currentTimeMillis()}.zip")
        val manager: DownloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }
}