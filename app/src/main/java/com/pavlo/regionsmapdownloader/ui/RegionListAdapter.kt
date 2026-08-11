package com.pavlo.regionsmapdownloader.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pavlo.regionsmapdownloader.R
import com.pavlo.regionsmapdownloader.domain.model.Region

class RegionListAdapter(
    private var items: List<RegionListItem>,
    private val onDownloadClick: (Region) -> Unit,
    private val onRegionClick: (RegionListItem.RegionRow) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun updateItems(newItems: List<RegionListItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is RegionListItem.ContinentHeader -> VIEW_TYPE_HEADER
        is RegionListItem.RegionRow -> VIEW_TYPE_REGION
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_HEADER) {
            ContinentHeaderViewHolder(inflater.inflate(R.layout.continent_header_item, parent, false))
        } else {
            RegionViewHolder(inflater.inflate(R.layout.region_list_item, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is RegionListItem.ContinentHeader -> (holder as ContinentHeaderViewHolder).bind(item)
            is RegionListItem.RegionRow -> (holder as RegionViewHolder).bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ContinentHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.continentNameTextView)

        fun bind(header: RegionListItem.ContinentHeader) {
            nameTextView.text = header.name
        }
    }

    inner class RegionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView? = itemView.findViewById(R.id.regionTitleTextView)
        private val mapButton: ImageButton? = itemView.findViewById(R.id.regionMapImageButton)

        fun bind(row: RegionListItem.RegionRow) {
            title?.text = row.region.name
            mapButton?.setOnClickListener { onDownloadClick(row.region) }
            itemView.setOnClickListener { onRegionClick(row) }
        }
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_REGION = 1
    }
}
