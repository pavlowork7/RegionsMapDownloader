package com.pavlo.regionsmapdownloader.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pavlo.regionsmapdownloader.R
import com.pavlo.regionsmapdownloader.domain.model.Region

class RegionsRecyclerViewAdapter(
    private var items: List<Region>,
    private val onItemClick: (Region) -> Unit,
): RecyclerView.Adapter<RegionsRecyclerViewAdapter.RegionViewHolder>() {

    fun updateItems(newItems: List<Region>) {
        items = newItems
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.region_list_item, parent, false)
        return RegionViewHolder(view)
    }

    override fun onBindViewHolder(holder: RegionViewHolder, position: Int) {
        val item = items[position]
        holder.title?.text = item.name
        holder.mapButton?.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class RegionViewHolder(item: View): RecyclerView.ViewHolder(item) {
        val title: TextView? = item.findViewById(R.id.regionTitleTextView)
        val mapButton: ImageButton? = item.findViewById(R.id.regionMapImageButton)
    }
}