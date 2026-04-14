package com.masterapp.queueeaseapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.masterapp.queueeaseapp.R
import com.masterapp.queueeaseapp.model.CenterResponse

class CenterAdapter(
    private val centerList: List<CenterResponse>,
    private val onCenterClick: (Long) -> Unit
) : RecyclerView.Adapter<CenterAdapter.CenterViewHolder>() {

    class CenterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvCenterName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CenterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_center, parent, false)
        return CenterViewHolder(view)
    }

    override fun onBindViewHolder(holder: CenterViewHolder, position: Int) {

        val center = centerList[position]

        holder.tvName.text = center.name ?: "No Name"

        // ✅ CLICK HANDLING (THIS IS YOUR ERROR FIX)
        holder.itemView.setOnClickListener {
            onCenterClick(center.id)
        }
    }

    override fun getItemCount(): Int = centerList.size
}