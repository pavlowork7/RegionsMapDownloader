package com.pavlo.regionsmapdownloader.domain.data_source

import com.pavlo.regionsmapdownloader.domain.model.DeviceMemoryInfo

interface DeviceStorageDataSource {
    suspend fun getDeviceMemoryInfo(): DeviceMemoryInfo
}