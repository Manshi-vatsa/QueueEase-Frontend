package com.masterapp.queueeaseapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.masterapp.queueeaseapp.R
import com.masterapp.queueeaseapp.model.CenterResponse

class CenterAdapter(
    private val onCenterClick: (CenterResponse) -> Unit
) : ListAdapter<CenterResponse, CenterAdapter.CenterViewHolder>(CenterDiffCallback) {

    class CenterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvCenterName)
        val tvLocation: TextView = itemView.findViewById(R.id.tvCenterLocation)
        val tvType: TextView = itemView.findViewById(R.id.tvCenterType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CenterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_center, parent, false)
        return CenterViewHolder(view)
    }

    override fun onBindViewHolder(holder: CenterViewHolder, position: Int) {

        val center = getItem(position)

        holder.tvName.text = center.name ?: "No Name"
        holder.tvLocation.text = center.location ?: "Unknown location"
        holder.tvType.text = center.type ?: "Unknown type"
        holder.itemView.setOnClickListener {
            onCenterClick(center)
        }
    }

    private object CenterDiffCallback : DiffUtil.ItemCallback<CenterResponse>() {
        override fun areItemsTheSame(oldItem: CenterResponse, newItem: CenterResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CenterResponse, newItem: CenterResponse): Boolean {
            return oldItem == newItem
        }
    }
}