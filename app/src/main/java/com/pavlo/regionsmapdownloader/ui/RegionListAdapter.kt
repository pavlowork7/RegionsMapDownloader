package com.pavlo.regionsmapdownloader.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pavlo.regionsmapdownloader.R
import com.pavlo.regionsmapdownloader.databinding.ContinentHeaderItemBinding
import com.pavlo.regionsmapdownloader.databinding.RegionListItemBinding

class RegionListAdapter(
    private val onDownloadClick: (regionKey: String, downloadName: String) -> Unit,
    private val onCancelClick: (regionKey: String) -> Unit,
    private val onRegionClick: (RegionListItem.RegionRow) -> Unit,
) : ListAdapter<RegionListItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is RegionListItem.ContinentHeader -> VIEW_TYPE_HEADER
        is RegionListItem.RegionRow -> VIEW_TYPE_REGION
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_HEADER) {
            ContinentHeaderViewHolder(ContinentHeaderItemBinding.inflate(inflater, parent, false))
        } else {
            RegionViewHolder(RegionListItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is RegionListItem.ContinentHeader -> (holder as ContinentHeaderViewHolder).bind(item)
            is RegionListItem.RegionRow -> (holder as RegionViewHolder).bind(item)
        }
    }

    class ContinentHeaderViewHolder(
        private val binding: ContinentHeaderItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(header: RegionListItem.ContinentHeader) {
            binding.continentNameTextView.text = header.name
        }
    }

    inner class RegionViewHolder(
        private val binding: RegionListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(row: RegionListItem.RegionRow) {
            val isDownloading = row.progress != null

            binding.regionTitleTextView.text = row.region.displayName
            binding.regionMapImageButton.visibility = if (row.region.isMap) View.VISIBLE else View.GONE
            binding.regionMapImageButton.setOnClickListener {
                if (isDownloading) onCancelClick(row.downloadKey) else onDownloadClick(row.downloadKey, row.region.downloadName)
            }
            binding.regionCardContainer.setOnClickListener { onRegionClick(row) }

            binding.linearProgress.progress = row.progress ?: 0
            binding.linearProgress.visibility = if (isDownloading) View.VISIBLE else View.GONE

            applyCompletedTint(row.isCompleted)
            applyDownloadIcon(isDownloading)
        }

        private fun applyCompletedTint(isCompleted: Boolean) {
            if (isCompleted) {
                binding.regionMapIconImageView.setColorFilter(
                    ContextCompat.getColor(binding.root.context, R.color.green_success)
                )
            } else {
                binding.regionMapIconImageView.clearColorFilter()
            }
        }

        private fun applyDownloadIcon(isDownloading: Boolean) {
            binding.regionMapImageButton.setImageResource(
                if (isDownloading) R.drawable.ic_action_remove_dark else R.drawable.ic_action_import
            )
            binding.regionMapImageButton.contentDescription = binding.root.context.getString(
                if (isDownloading) R.string.cancel_download_icon_content_description
                else R.string.download_image_icon_content_description
            )
        }
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_REGION = 1

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RegionListItem>() {
            override fun areItemsTheSame(oldItem: RegionListItem, newItem: RegionListItem): Boolean = when {
                oldItem is RegionListItem.ContinentHeader && newItem is RegionListItem.ContinentHeader ->
                    oldItem.name == newItem.name
                oldItem is RegionListItem.RegionRow && newItem is RegionListItem.RegionRow ->
                    oldItem.downloadKey == newItem.downloadKey
                else -> false
            }

            override fun areContentsTheSame(oldItem: RegionListItem, newItem: RegionListItem): Boolean =
                oldItem == newItem
        }
    }
}
