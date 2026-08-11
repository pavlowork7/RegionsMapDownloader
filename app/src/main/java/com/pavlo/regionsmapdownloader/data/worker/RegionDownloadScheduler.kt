package com.pavlo.regionsmapdownloader.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.pavlo.regionsmapdownloader.domain.model.Region
import java.util.UUID

class RegionDownloadScheduler(private val context: Context) {
    fun schedule(regionName: String, url: String, destinationPath: String): UUID {
        val workRequest = OneTimeWorkRequestBuilder<RegionDownloadWorker>()
            .setInputData(workDataOf(
                RegionDownloadWorker.KEY_URL to url,
                RegionDownloadWorker.KEY_DEST to "${context.filesDir}/${destinationPath}.obf"
            ))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            regionName, ExistingWorkPolicy.REPLACE, workRequest
        )
        return workRequest.id
    }
}