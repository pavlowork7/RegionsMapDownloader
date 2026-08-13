package com.pavlo.regionsmapdownloader

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.pavlo.regionsmapdownloader.data.data_source.DeviceStorageDataSourceImpl
import com.pavlo.regionsmapdownloader.data.data_source.MapStorageImpl
import com.pavlo.regionsmapdownloader.data.data_source.RegionDownloadDataSourceImpl
import com.pavlo.regionsmapdownloader.data.queue.DownloadQueue
import com.pavlo.regionsmapdownloader.data.queue.SharedPrefsDownloadQueueStorage
import com.pavlo.regionsmapdownloader.data.repository.RegionRepositoryImpl
import com.pavlo.regionsmapdownloader.data.worker.DownloadQueueWorker
import com.pavlo.regionsmapdownloader.data.worker.RegionDownloadScheduler
import com.pavlo.regionsmapdownloader.domain.data_source.DeviceStorageDataSource
import com.pavlo.regionsmapdownloader.domain.data_source.DownloadQueueStorage
import com.pavlo.regionsmapdownloader.domain.data_source.MapStorage
import com.pavlo.regionsmapdownloader.domain.data_source.RegionDownloadDataSource
import com.pavlo.regionsmapdownloader.domain.repository.RegionRepository
import com.pavlo.regionsmapdownloader.domain.usecase.DownloadRegionUseCase
import com.pavlo.regionsmapdownloader.domain.usecase.GetAllRegionsUseCase
import com.pavlo.regionsmapdownloader.domain.usecase.GetDeviceMemoryInfoUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import okhttp3.OkHttpClient
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class AppInitializer(context: Context) {

    private val appContext = context.applicationContext

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_VALUE, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_VALUE, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_VALUE, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Один потік на всі завантаження карт — саме на ньому виконується мережеве й файлове
     * введення-виведення. Разом із чергою це і є «послідовне завантаження в один потік».
     */
    val downloadDispatcher: CoroutineDispatcher by lazy {
        Executors.newSingleThreadExecutor { runnable -> Thread(runnable, DOWNLOAD_THREAD_NAME) }
            .asCoroutineDispatcher()
    }

    val regionRepository: RegionRepository by lazy {
        RegionRepositoryImpl(appContext)
    }

    val deviceStorageDataSource: DeviceStorageDataSource by lazy {
        DeviceStorageDataSourceImpl(appContext)
    }

    val mapStorage: MapStorage by lazy {
        MapStorageImpl(appContext)
    }

    val downloadQueueStorage: DownloadQueueStorage by lazy {
        SharedPrefsDownloadQueueStorage(appContext)
    }

    val downloadQueue: DownloadQueue by lazy {
        DownloadQueue(downloadQueueStorage, mapStorage)
    }

    val regionDownloadDataSource: RegionDownloadDataSource by lazy {
        RegionDownloadDataSourceImpl(okHttpClient, downloadDispatcher)
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
        RegionDownloadScheduler(appContext, downloadQueue)
    }

    val workerFactory: WorkerFactory by lazy {
        object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ): ListenableWorker? {
                return if (workerClassName == DownloadQueueWorker::class.java.name) {
                    DownloadQueueWorker(
                        appContext,
                        workerParameters,
                        downloadQueue,
                        downloadRegionUseCase,
                        mapStorage
                    )
                } else {
                    null
                }
            }
        }
    }

    companion object {
        const val CONNECT_TIMEOUT_VALUE = 15L
        const val READ_TIMEOUT_VALUE = 60L
        const val WRITE_TIMEOUT_VALUE = 15L
        private const val DOWNLOAD_THREAD_NAME = "map-download"
    }
}
