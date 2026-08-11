package com.pavlo.regionsmapdownloader.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.pavlo.regionsmapdownloader.R
import com.pavlo.regionsmapdownloader.domain.model.Region

class RegionListAdapter(
    private var items: List<RegionListItem>,
    private val onDownloadClick: (String, String, String) -> Unit,
    private val onRegionClick: (RegionListItem.RegionRow) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var downloadProgressMap: Map<String, Int> = emptyMap()
    private val completedRegions = mutableSetOf<String>()

    fun setDownloadProgress(progress: Map<String, Int>) {
        val changedNames = (downloadProgressMap.keys + progress.keys)
            .filter { downloadProgressMap[it] != progress[it] }
        downloadProgressMap = progress
        changedNames.forEach { name ->
            val position = items.indexOfFirst {
                it is RegionListItem.RegionRow && it.region.name == name
            }
            if (position != -1) {
                notifyItemChanged(position, PAYLOAD_PROGRESS)
            }
        }
    }

    fun markCompleted(name: String) {
        if (!completedRegions.add(name)) return
        val position = items.indexOfFirst {
            it is RegionListItem.RegionRow && it.region.name == name
        }
        if (position != -1) {
            notifyItemChanged(position, PAYLOAD_PROGRESS)
        }
    }

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

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }

        val item = items[position]
        if (item is RegionListItem.RegionRow && holder is RegionViewHolder) {
            holder.progressBar?.progress = downloadProgressMap[item.region.name] ?: 0
            holder.progressBar?.visibility = if (item.region.name in downloadProgressMap) View.VISIBLE else View.GONE
            holder.applyCompletedTint(item.region.name in completedRegions)
        }
    }

    override fun getItemCount(): Int = items.size

    class ContinentHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.continentNameTextView)

        fun bind(header: RegionListItem.ContinentHeader) {
            nameTextView.text = header.name
        }
    }

    inner class RegionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView? = itemView.findViewById(R.id.regionTitleTextView)
        private val mapButton: ImageButton? = itemView.findViewById(R.id.regionMapImageButton)
        private val mapIcon: ImageView? = itemView.findViewById(R.id.regionMapIconImageView)
        val progressBar: LinearProgressIndicator? = itemView.findViewById(R.id.linearProgress)

        fun bind(row: RegionListItem.RegionRow) {
            title?.text = row.region.displayName
            mapButton?.setOnClickListener { onDownloadClick(row.region.name, row.downloadUrl, row.getFileName()) }
            itemView.setOnClickListener { onRegionClick(row) }

            progressBar?.progress = downloadProgressMap[row.region.name] ?: 0
            progressBar?.visibility = if (row.region.name in downloadProgressMap) View.VISIBLE else View.GONE
            applyCompletedTint(row.region.name in completedRegions)
        }

        fun applyCompletedTint(isCompleted: Boolean) {
            if (isCompleted) {
                mapIcon?.setColorFilter(ContextCompat.getColor(itemView.context, R.color.green_success))
            } else {
                mapIcon?.clearColorFilter()
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_REGION = 1
        private const val PAYLOAD_PROGRESS = "payload_progress"
    }
}
