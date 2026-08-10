package com.pavlo.regionsmapdownloader

import android.content.Context
import com.pavlo.regionsmapdownloader.data.data_source.DeviceStorageDataSourceImpl
import com.pavlo.regionsmapdownloader.data.repository.RegionRepositoryImpl
import com.pavlo.regionsmapdownloader.domain.data_source.DeviceStorageDataSource
import com.pavlo.regionsmapdownloader.domain.repository.RegionRepository
import com.pavlo.regionsmapdownloader.domain.usecase.GetAllRegionsUseCase
import com.pavlo.regionsmapdownloader.domain.usecase.GetDeviceMemoryInfoUseCase

class AppInitializer(context: Context) {
    private val appContext = context.applicationContext

    val regionRepository: RegionRepository by lazy {
        RegionRepositoryImpl(appContext)
    }

    val deviceStorageDataSource: DeviceStorageDataSource by lazy {
        DeviceStorageDataSourceImpl()
    }

    val getAllRegionsUseCase: GetAllRegionsUseCase by lazy {
        GetAllRegionsUseCase(regionRepository)
    }

    val getDeviceMemoryInfoUseCase: GetDeviceMemoryInfoUseCase by lazy {
        GetDeviceMemoryInfoUseCase(deviceStorageDataSource)
    }
}