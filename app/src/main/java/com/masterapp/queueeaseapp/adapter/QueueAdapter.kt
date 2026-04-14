package com.masterapp.queueeaseapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.masterapp.queueeaseapp.R
import com.masterapp.queueeaseapp.model.QueueUser

class QueueAdapter(private val list: List<QueueUser>) :
    RecyclerView.Adapter<QueueAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userId: TextView = view.findViewById(R.id.tvUserId)
        val queueNumber: TextView = view.findViewById(R.id.tvQueueNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_queue_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.userId.text = "User: ${item.userId}"
        holder.queueNumber.text = "Queue No: ${item.queueNumber}"
    }

    override fun getItemCount(): Int = list.size
}