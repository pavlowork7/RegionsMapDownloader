package com.pavlo.regionsmapdownloader.domain.data_source

import com.pavlo.regionsmapdownloader.domain.model.DownloadQueueItem

/** Персистенція черги: вона має пережити смерть процесу і перезапуск воркера. */
interface DownloadQueueStorage {
    fun load(): List<DownloadQueueItem>
    fun save(items: List<DownloadQueueItem>)
}
