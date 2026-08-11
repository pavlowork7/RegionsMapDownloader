package com.pavlo.regionsmapdownloader.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.pavlo.regionsmapdownloader.domain.model.DownloadProgress
import com.pavlo.regionsmapdownloader.domain.usecase.DownloadRegionUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

class RegionDownloadWorker(
    context: Context,
    params: WorkerParameters,
    private val downloadRegionUseCase: DownloadRegionUseCase
): CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = downloadQueue.withLock {
        val url = inputData.getString(KEY_URL) ?: return@withLock Result.failure()
        val destination = inputData.getString(KEY_DEST) ?: return@withLock Result.failure()

        try {
            var lastReportedPercent = -1
            downloadRegionUseCase(url, File(destination)).collect { downloadProgress ->
                if (downloadProgress is DownloadProgress.InProgress && downloadProgress.percent != lastReportedPercent) {
                    lastReportedPercent = downloadProgress.percent
                    setProgress(workDataOf(KEY_PROGRESS to downloadProgress.percent))
                }
            }
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val KEY_URL = "url"
        const val KEY_DEST = "destination"
        const val KEY_PROGRESS = "progress"

        private val downloadQueue = Mutex()
    }
}