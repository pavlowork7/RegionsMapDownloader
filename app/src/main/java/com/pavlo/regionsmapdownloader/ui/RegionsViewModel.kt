package com.pavlo.regionsmapdownloader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.pavlo.regionsmapdownloader.data.worker.RegionDownloadScheduler
import com.pavlo.regionsmapdownloader.data.worker.RegionDownloadWorker
import com.pavlo.regionsmapdownloader.domain.model.DeviceMemoryInfo
import com.pavlo.regionsmapdownloader.domain.model.Region
import com.pavlo.regionsmapdownloader.domain.usecase.GetAllRegionsUseCase
import com.pavlo.regionsmapdownloader.domain.usecase.GetDeviceMemoryInfoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class RegionsListUiState {
    data object Loading: RegionsListUiState()
    data class Success(val regions: List<Region>): RegionsListUiState()
    data class Error(val message: String): RegionsListUiState()
}

class RegionsViewModel(
    private val getAllRegionsUseCase: GetAllRegionsUseCase,
    private val getDeviceMemoryInfoUseCase: GetDeviceMemoryInfoUseCase,
    private val downloadScheduler: RegionDownloadScheduler,
    private val workManager: WorkManager
): ViewModel() {
    private val _state = MutableStateFlow<RegionsListUiState>(RegionsListUiState.Loading)
    val state: StateFlow<RegionsListUiState> = _state.asStateFlow()

    private val _memoryInfo = MutableStateFlow<DeviceMemoryInfo?>(null)
    val memoryInfo: StateFlow<DeviceMemoryInfo?> = _memoryInfo.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Int>> = _downloadProgress.asStateFlow()

    private val _completedRegions = MutableStateFlow<Set<String>>(emptySet())
    val completedRegions: StateFlow<Set<String>> = _completedRegions.asStateFlow()

    fun loadRegions() {
        viewModelScope.launch {
            getAllRegionsUseCase().fold(
                onSuccess = { regions ->
                    _state.value = RegionsListUiState.Success(regions)
                },
                onFailure = { error ->
                    _state.value = RegionsListUiState.Error(message = error.toString())
                }
            )
        }
    }

    fun loadMemoryInfo() {
        viewModelScope.launch {
            _memoryInfo.value = getDeviceMemoryInfoUseCase()
        }
    }

    fun startDownload(regionName: String, url: String, destinationPath: String) {
        val workId = downloadScheduler.schedule(regionName, url, destinationPath)
        _downloadProgress.update { it + (regionName to 0) }

        viewModelScope.launch {
            workManager.getWorkInfoByIdFlow(workId).collect { workInfo ->
                when (workInfo?.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        _completedRegions.update { it + regionName }
                        _downloadProgress.update { it - regionName }
                    }
                    WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                        _downloadProgress.update { it - regionName }
                    }
                    else -> {
                        val percent = workInfo?.progress?.getInt(RegionDownloadWorker.KEY_PROGRESS, 0) ?: 0
                        _downloadProgress.update { it + (regionName to percent) }
                    }
                }
            }
        }
    }
}