package com.pavlo.regionsmapdownloader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.pavlo.regionsmapdownloader.data.worker.RegionDownloadScheduler
import com.pavlo.regionsmapdownloader.domain.usecase.GetAllRegionsUseCase
import com.pavlo.regionsmapdownloader.domain.usecase.GetDeviceMemoryInfoUseCase

class RegionsViewModelFactory(
    private val getAllRegionsUseCase: GetAllRegionsUseCase,
    private val getDeviceMemoryInfoUseCase: GetDeviceMemoryInfoUseCase,
    private val regionDownloadScheduler: RegionDownloadScheduler,
    private val workManager: WorkManager
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegionsViewModel(
                getAllRegionsUseCase,
                getDeviceMemoryInfoUseCase,
                regionDownloadScheduler,
                workManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}