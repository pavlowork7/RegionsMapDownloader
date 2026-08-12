package com.pavlo.regionsmapdownloader.data.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.pavlo.regionsmapdownloader.data.data_source.OsmAndUrlBuilder
import java.io.File
import java.util.concurrent.TimeUnit

class RegionDownloadScheduler(private val context: Context) {

    fun enqueue(regionKey: String, downloadName: String) {
        val request = OneTimeWorkRequestBuilder<RegionDownloadWorker>()
            .setInputData(
                workDataOf(
                    RegionDownloadWorker.KEY_URL to OsmAndUrlBuilder.mapUrl(downloadName),
                    RegionDownloadWorker.KEY_DEST to destinationFile(downloadName).path
                )
            )
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_DELAY_SECONDS, TimeUnit.SECONDS)
            .addTag(TAG_DOWNLOAD)
            .addTag(regionTag(regionKey))
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(regionKey, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(regionKey: String) {
        WorkManager.getInstance(context).cancelUniqueWork(regionKey)
    }

    private fun destinationFile(downloadName: String): File {
        val mapsDir = context.getExternalFilesDir(MAPS_DIR) ?: File(context.filesDir, MAPS_DIR)
        mapsDir.mkdirs()
        return File(mapsDir, "$downloadName.obf")
    }

    companion object {
        const val TAG_DOWNLOAD = "region_download"
        private const val MAPS_DIR = "osmand"
        private const val BACKOFF_DELAY_SECONDS = 10L

        fun regionTag(regionKey: String) = "region:$regionKey"
    }
}
