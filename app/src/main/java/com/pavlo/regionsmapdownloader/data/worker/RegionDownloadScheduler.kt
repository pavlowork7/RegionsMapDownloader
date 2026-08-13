package com.pavlo.regionsmapdownloader.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.pavlo.regionsmapdownloader.data.queue.DownloadQueue
import com.pavlo.regionsmapdownloader.domain.model.DownloadQueueItem

class RegionDownloadScheduler(
    private val context: Context,
    private val downloadQueue: DownloadQueue
) {
    suspend fun enqueue(item: DownloadQueueItem) {
        if (downloadQueue.enqueue(item)) {
            startQueueWorker()
        }
    }

    suspend fun cancel(downloadName: String) {
        downloadQueue.cancel(downloadName)
    }

    private fun startQueueWorker() {
        val request = OneTimeWorkRequestBuilder<DownloadQueueWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(QUEUE_WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }

    private companion object {
        const val QUEUE_WORK_NAME = "region_download_queue"
    }
}
