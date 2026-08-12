package com.pavlo.regionsmapdownloader.data.data_source

import android.content.Context
import android.os.StatFs
import com.pavlo.regionsmapdownloader.domain.data_source.DeviceStorageDataSource
import com.pavlo.regionsmapdownloader.domain.model.DeviceMemoryInfo

class DeviceStorageDataSourceImpl(private val context: Context) : DeviceStorageDataSource {

    override suspend fun getDeviceMemoryInfo(): DeviceMemoryInfo {
        val storagePath = (context.getExternalFilesDir(null) ?: context.filesDir).path
        val stat = StatFs(storagePath)
        val totalBytes = stat.totalBytes
        val freeBytes = stat.availableBytes
        val usedPercent = if (totalBytes > 0) {
            (((totalBytes - freeBytes) * 100) / totalBytes).toInt()
        } else {
            0
        }

        return DeviceMemoryInfo(usedPercent, freeBytes)
    }
}