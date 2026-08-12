package com.pavlo.regionsmapdownloader.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.pavlo.regionsmapdownloader.R

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
}
