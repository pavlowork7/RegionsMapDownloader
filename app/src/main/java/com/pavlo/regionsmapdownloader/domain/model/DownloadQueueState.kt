package com.pavlo.regionsmapdownloader.domain.model

/**
 * Незмінний знімок черги: рівно одне активне завантаження ([active]) і решта, що чекають ([pending]).
 *
 * Незмінність тут не косметична — саме вона дозволяє оновлювати чергу через
 * атомарний CAS у `MutableStateFlow.update`, без блокувань і без спільного мутабельного стану.
 */
data class DownloadQueueState(
    val active: DownloadQueueItem? = null,
    val activePercent: Int = 0,
    val pending: List<DownloadQueueItem> = emptyList()
) {
    /** Усі елементи в порядку обробки — активний першим. Саме в такому вигляді черга персиститься. */
    val items: List<DownloadQueueItem>
        get() = listOfNotNull(active) + pending

    /** Прогрес за іменем завантаження: активне — реальний відсоток, ті, що чекають — 0. */
    val progressByName: Map<String, Int>
        get() = buildMap {
            pending.forEach { put(it.downloadName, 0) }
            active?.let { put(it.downloadName, activePercent) }
        }

    fun contains(downloadName: String): Boolean =
        active?.downloadName == downloadName || pending.any { it.downloadName == downloadName }
}
