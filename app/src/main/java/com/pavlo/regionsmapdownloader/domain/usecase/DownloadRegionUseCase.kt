package com.pavlo.regionsmapdownloader.domain.usecase

import com.pavlo.regionsmapdownloader.domain.data_source.RegionDownloadDataSource
import com.pavlo.regionsmapdownloader.domain.model.DownloadProgress
import kotlinx.coroutines.flow.Flow
import java.io.File

class DownloadRegionUseCase(
    private val regionDownloadDataSource: RegionDownloadDataSource
) {
    operator fun invoke(url: String, destination: File): Flow<DownloadProgress> {
        return regionDownloadDataSource.downloadMap(url, destination)
    }
}