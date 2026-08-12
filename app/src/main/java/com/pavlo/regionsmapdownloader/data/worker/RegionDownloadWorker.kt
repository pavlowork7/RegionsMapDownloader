package com.pavlo.regionsmapdownloader.data.worker

import android.content.Context
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.pavlo.regionsmapdownloader.R
import com.pavlo.regionsmapdownloader.domain.model.DownloadProgress
import com.pavlo.regionsmapdownloader.domain.usecase.DownloadRegionUseCase
import kotlinx.coroutines.CancellationException
import java.io.File
import java.io.IOException

class RegionDownloadWorker(
    context: Context,
    params: WorkerParameters,
    private val downloadRegionUseCase: DownloadRegionUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val url = inputData.getString(KEY_URL) ?: return Result.failure()
        val destination = inputData.getString(KEY_DEST) ?: return Result.failure()

        return try {
            setForeground(createForegroundInfo(0))

            var lastReportedPercent = -1
            downloadRegionUseCase(url, File(destination)).collect { downloadProgress ->
                if (downloadProgress is DownloadProgress.InProgress && downloadProgress.percent != lastReportedPercent) {
                    lastReportedPercent = downloadProgress.percent
                    setProgress(workDataOf(KEY_PROGRESS to downloadProgress.percent))
                    setForeground(createForegroundInfo(downloadProgress.percent))
                }
            }
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            if (runAttemptCount + 1 < MAX_ATTEMPTS) Result.retry()
            else Result.failure(workDataOf(KEY_ERROR to e.message))
        } catch (e: Exception) {
            Result.failure(workDataOf(KEY_ERROR to e.message))
        }
    }

    private fun createForegroundInfo(percent: Int): ForegroundInfo {
        val cancelIntent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)
        val notification = NotificationCompat.Builder(applicationContext, DownloadNotifications.CHANNEL_ID)
            .setContentTitle(applicationContext.getString(R.string.downloading_map))
            .setSmallIcon(R.drawable.ic_action_import)
            .setProgress(HUNDRED_PERCENT, percent, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(0, applicationContext.getString(R.string.cancel), cancelIntent)
            .build()

        return ForegroundInfo(
            DownloadNotifications.NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    companion object {
        const val KEY_URL = "url"
        const val KEY_DEST = "destination"
        const val KEY_PROGRESS = "progress"
        const val KEY_ERROR = "error"
        private const val MAX_ATTEMPTS = 3
        private const val HUNDRED_PERCENT = 100
    }
}
