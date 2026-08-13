package com.pavlo.regionsmapdownloader.data.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.pavlo.regionsmapdownloader.R
import com.pavlo.regionsmapdownloader.domain.model.DownloadQueueItem

/**
 * Сповіщення одне на всю чергу — це коректно саме тому, що завантаження послідовні:
 * у будь-який момент є рівно одна активна карта, а решта показані лічильником.
 */
object DownloadNotifications {
    const val CHANNEL_ID = "region_download_channel"
    const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.download_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.download_notification_channel_description)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun buildProgressNotification(
        context: Context,
        item: DownloadQueueItem,
        percent: Int,
        queuedCount: Int
    ): Notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle(context.getString(R.string.downloading_map_format, item.displayName))
        .setContentText(
            if (queuedCount > 0) {
                context.resources.getQuantityString(R.plurals.downloads_queued, queuedCount, queuedCount)
            } else {
                null
            }
        )
        .setSmallIcon(R.drawable.ic_action_import)
        .setProgress(HUNDRED_PERCENT, percent, false)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .addAction(
            0,
            context.getString(R.string.cancel),
            DownloadCancelReceiver.pendingIntent(context, item.downloadName)
        )
        .build()

    private const val HUNDRED_PERCENT = 100
}
