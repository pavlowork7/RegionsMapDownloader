package com.pavlo.regionsmapdownloader.data.worker

import android.content.Context
import android.content.pm.ServiceInfo
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.pavlo.regionsmapdownloader.data.data_source.OsmAndUrlBuilder
import com.pavlo.regionsmapdownloader.data.queue.DownloadQueue
import com.pavlo.regionsmapdownloader.domain.data_source.MapStorage
import com.pavlo.regionsmapdownloader.domain.model.DownloadOutcome
import com.pavlo.regionsmapdownloader.domain.model.DownloadProgress
import com.pavlo.regionsmapdownloader.domain.model.DownloadQueueItem
import com.pavlo.regionsmapdownloader.domain.usecase.DownloadRegionUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Єдиний воркер, який розбирає чергу завантажень.
 *
 * Послідовність тут не від блокувань, а від самої структури: воркер один
 * (унікальна робота WorkManager), і він крутить `takeNext() → download → finishActive()`,
 * доки черга не спорожніє. Другого завантаження одночасно просто нема кому запустити.
 */
class DownloadQueueWorker(
    context: Context,
    params: WorkerParameters,
    private val downloadQueue: DownloadQueue,
    private val downloadRegionUseCase: DownloadRegionUseCase,
    private val mapStorage: MapStorage
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        downloadQueue.restore()

        var item = downloadQueue.takeNext()
        while (item != null) {
            updateForeground(item, 0)
            downloadQueue.finishActive(downloadWithRetry(item))
            item = downloadQueue.takeNext()
        }
        return Result.success()
    }

    private suspend fun downloadWithRetry(item: DownloadQueueItem): DownloadOutcome {
        var attempt = 1
        while (true) {
            val outcome = downloadItem(item)
            if (outcome !is DownloadOutcome.Failure || attempt >= MAX_ATTEMPTS) return outcome
            delay(RETRY_DELAY_MILLIS * attempt)
            attempt++
        }
    }

    /**
     * Качає один елемент у власній корутині, щоб скасування зупинило саме його,
     * а не воркер разом з рештою черги.
     */
    private suspend fun downloadItem(item: DownloadQueueItem): DownloadOutcome = coroutineScope {
        val download = async { runDownload(item) }
        val cancellationWatcher = launch {
            // Елемент перестав бути активним — отже, його скасували.
            downloadQueue.state.first { it.active?.downloadName != item.downloadName }
            download.cancel()
        }

        try {
            download.await()
        } catch (e: CancellationException) {
            // Якщо зупиняють увесь воркер — виходимо, а не «ковтаємо» скасування.
            currentCoroutineContext().ensureActive()
            DownloadOutcome.Cancelled
        } finally {
            cancellationWatcher.cancel()
        }
    }

    private suspend fun runDownload(item: DownloadQueueItem): DownloadOutcome = try {
        var lastNotifiedPercent = -1
        downloadRegionUseCase(
            url = OsmAndUrlBuilder.mapUrl(item.downloadName),
            destination = mapStorage.mapFile(item.downloadName)
        ).collect { progress ->
            if (progress is DownloadProgress.InProgress) {
                downloadQueue.updateProgress(progress.percent)
                if (progress.percent - lastNotifiedPercent >= NOTIFICATION_STEP_PERCENT) {
                    lastNotifiedPercent = progress.percent
                    updateForeground(item, progress.percent)
                }
            }
        }
        DownloadOutcome.Success
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        DownloadOutcome.Failure(e.message)
    }

    /**
     * Сповіщення — допоміжне. Якщо система не дала стартувати foreground service
     * (застосунок у фоні на Android 12+) або немає дозволу на сповіщення,
     * завантаження має тривати, а не падати.
     */
    private suspend fun updateForeground(item: DownloadQueueItem, percent: Int) {
        runCatching { setForeground(createForegroundInfo(item, percent)) }
    }

    private fun createForegroundInfo(item: DownloadQueueItem, percent: Int): ForegroundInfo {
        val notification = DownloadNotifications.buildProgressNotification(
            context = applicationContext,
            item = item,
            percent = percent,
            queuedCount = downloadQueue.state.value.pending.size
        )
        return ForegroundInfo(
            DownloadNotifications.NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    companion object {
        private const val MAX_ATTEMPTS = 3
        private const val RETRY_DELAY_MILLIS = 10_000L
        private const val NOTIFICATION_STEP_PERCENT = 5
    }
}
