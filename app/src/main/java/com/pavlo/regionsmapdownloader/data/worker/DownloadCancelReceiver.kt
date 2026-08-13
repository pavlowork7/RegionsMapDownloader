package com.pavlo.regionsmapdownloader.data.worker

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pavlo.regionsmapdownloader.RegionApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Кнопка «Cancel» у сповіщенні скасовує лише поточну карту, а не всю чергу:
 * воркер після цього просто бере наступний елемент.
 */
class DownloadCancelReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val downloadName = intent.getStringExtra(EXTRA_DOWNLOAD_NAME) ?: return
        val application = context.applicationContext as? RegionApplication ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                application.appInitializer.downloadQueue.cancel(downloadName)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val EXTRA_DOWNLOAD_NAME = "download_name"

        fun pendingIntent(context: Context, downloadName: String): PendingIntent {
            val intent = Intent(context, DownloadCancelReceiver::class.java)
                .putExtra(EXTRA_DOWNLOAD_NAME, downloadName)

            return PendingIntent.getBroadcast(
                context,
                downloadName.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
