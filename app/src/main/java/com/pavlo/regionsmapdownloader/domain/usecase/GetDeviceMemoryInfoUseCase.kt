package com.pavlo.regionsmapdownloader.domain.usecase

import com.pavlo.regionsmapdownloader.domain.data_source.DeviceStorageDataSource
import com.pavlo.regionsmapdownloader.domain.model.DeviceMemoryInfo

class GetDeviceMemoryInfoUseCase(
    private val dataSource: DeviceStorageDataSource
) {
    suspend operator fun invoke(): DeviceMemoryInfo {
        return dataSource.getDeviceMemoryInfo()
    }
}