package com.pavlo.regionsmapdownloader.domain.model

data class DownloadQueueState(
    val active: DownloadQueueItem? = null,
    val activePercent: Int = 0,
    val pending: List<DownloadQueueItem> = emptyList()
) {
    val items: List<DownloadQueueItem>
        get() = listOfNotNull(active) + pending

    val progressByName: Map<String, Int>
        get() = buildMap {
            pending.forEach { put(it.downloadName, 0) }
            active?.let { put(it.downloadName, activePercent) }
        }

    fun contains(downloadName: String): Boolean =
        active?.downloadName == downloadName || pending.any { it.downloadName == downloadName }
}
