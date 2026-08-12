package com.pavlo.regionsmapdownloader

import android.app.Application
import androidx.work.Configuration
import com.pavlo.regionsmapdownloader.data.worker.DownloadNotifications

class RegionApplication: Application(), Configuration.Provider {
    lateinit var appInitializer: AppInitializer
        private set

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(appInitializer.workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        appInitializer = AppInitializer(this)
        DownloadNotifications.createChannel(this)
    }
}
