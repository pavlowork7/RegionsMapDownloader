package com.pavlo.regionsmapdownloader

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.pavlo.regionsmapdownloader.data.data_source.DeviceStorageDataSourceImpl
import com.pavlo.regionsmapdownloader.data.data_source.RegionDownloadDataSourceImpl
import com.pavlo.regionsmapdownloader.data.repository.RegionRepositoryImpl
import com.pavlo.regionsmapdownloader.data.worker.RegionDownloadScheduler
import com.pavlo.regionsmapdownloader.data.worker.RegionDownloadWorker
import com.pavlo.regionsmapdownloader.domain.data_source.DeviceStorageDataSource
import com.pavlo.regionsmapdownloader.domain.data_source.RegionDownloadDataSource
import com.pavlo.regionsmapdownloader.domain.repository.RegionRepository
import com.pavlo.regionsmapdownloader.domain.usecase.DownloadRegionUseCase
import com.pavlo.regionsmapdownloader.domain.usecase.GetAllRegionsUseCase
import com.pavlo.regionsmapdownloader.domain.usecase.GetDeviceMemoryInfoUseCase
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class AppInitializer(context: Context) {


    private val appContext = context.applicationContext

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_VALUE, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_VALUE, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_VALUE, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    val regionRepository: RegionRepository by lazy {
        RegionRepositoryImpl(appContext)
    }

    val deviceStorageDataSource: DeviceStorageDataSource by lazy {
        DeviceStorageDataSourceImpl(appContext)
    }

    val regionDownloadDataSource: RegionDownloadDataSource by lazy {
        RegionDownloadDataSourceImpl(okHttpClient)
    }

    val getAllRegionsUseCase: GetAllRegionsUseCase by lazy {
        GetAllRegionsUseCase(regionRepository)
    }

    val getDeviceMemoryInfoUseCase: GetDeviceMemoryInfoUseCase by lazy {
        GetDeviceMemoryInfoUseCase(deviceStorageDataSource)
    }

    val downloadRegionUseCase: DownloadRegionUseCase by lazy {
        DownloadRegionUseCase(regionDownloadDataSource)
    }

    val regionDownloadScheduler: RegionDownloadScheduler by lazy {
        RegionDownloadScheduler(appContext)
    }

    val workerFactory: WorkerFactory by lazy {
        object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ): ListenableWorker? {
                return if (workerClassName == RegionDownloadWorker::class.java.name) {
                    RegionDownloadWorker(appContext, workerParameters, downloadRegionUseCase)
                } else {
                    null
                }
            }
        }
    }

    val workManager: WorkManager by lazy {
        WorkManager.getInstance(appContext)
    }

    companion object {
        const val TIMEOUT_VALUE = 15L
        const val READ_TIMEOUT_VALUE = 60L
        const val WRITE_TIMEOUT_VALUE = 15L
    }
}