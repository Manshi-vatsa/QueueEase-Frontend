package com.masterapp.queueeaseapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.masterapp.queueeaseapp.R
import com.masterapp.queueeaseapp.model.QueueUser

class QueueAdapter : ListAdapter<QueueUser, QueueAdapter.ViewHolder>(QueueDiffCallback) {

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
        val item = getItem(position)
        holder.userId.text = "User: ${item.userId}"
        holder.queueNumber.text = "Queue No: ${item.queueNumber}"
    }

    private object QueueDiffCallback : DiffUtil.ItemCallback<QueueUser>() {
        override fun areItemsTheSame(oldItem: QueueUser, newItem: QueueUser): Boolean {
            return oldItem.bookingId == newItem.bookingId
        }

        override fun areContentsTheSame(oldItem: QueueUser, newItem: QueueUser): Boolean {
            return oldItem == newItem
        }
    }
}