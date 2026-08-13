package com.pavlo.regionsmapdownloader.domain.data_source

import com.pavlo.regionsmapdownloader.domain.model.DownloadQueueItem

interface DownloadQueueStorage {
    fun load(): List<DownloadQueueItem>
    fun save(items: List<DownloadQueueItem>)
}
