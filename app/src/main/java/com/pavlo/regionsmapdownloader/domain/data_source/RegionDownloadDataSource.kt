package com.pavlo.regionsmapdownloader.domain.data_source

import com.pavlo.regionsmapdownloader.domain.model.DownloadProgress
import kotlinx.coroutines.flow.Flow
import java.io.File

interface RegionDownloadDataSource {
    fun downloadMap(url: String, destination: File): Flow<DownloadProgress>
}