package com.pavlo.regionsmapdownloader.domain.model

sealed interface DownloadQueueEvent {
    data class Failed(val item: DownloadQueueItem, val message: String?) : DownloadQueueEvent
}
