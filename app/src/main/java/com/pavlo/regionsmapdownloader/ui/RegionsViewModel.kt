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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

sealed class RegionsListUiState {
    data object Loading : RegionsListUiState()
    data class Success(val regions: List<Region>) : RegionsListUiState()
    data class Error(val message: String) : RegionsListUiState()
}

sealed interface RegionsEvent {
    data class DownloadFailed(val regionKey: String, val downloadName: String, val message: String?) : RegionsEvent
}

class RegionsViewModel(
    private val getAllRegionsUseCase: GetAllRegionsUseCase,
    private val getDeviceMemoryInfoUseCase: GetDeviceMemoryInfoUseCase,
    private val downloadScheduler: RegionDownloadScheduler,
    private val workManager: WorkManager
) : ViewModel() {
    private val _state = MutableStateFlow<RegionsListUiState>(RegionsListUiState.Loading)
    val state: StateFlow<RegionsListUiState> = _state.asStateFlow()

    private val _memoryInfo = MutableStateFlow<DeviceMemoryInfo?>(null)
    val memoryInfo: StateFlow<DeviceMemoryInfo?> = _memoryInfo.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Int>> = _downloadProgress.asStateFlow()

    private val _completedRegions = MutableStateFlow<Set<String>>(emptySet())
    val completedRegions: StateFlow<Set<String>> = _completedRegions.asStateFlow()

    private val _events = MutableSharedFlow<RegionsEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<RegionsEvent> = _events.asSharedFlow()

    private val downloadNames = mutableMapOf<String, String>()
    private val processedTerminalIds = mutableSetOf<UUID>()

    init {
        observeDownloads()
    }

    private fun observeDownloads() {
        viewModelScope.launch {
            workManager.getWorkInfosByTagFlow(RegionDownloadScheduler.TAG_DOWNLOAD).collect { infos ->
                infos.forEach(::handleWorkInfo)
            }
        }
    }

    private fun handleWorkInfo(info: WorkInfo) {
        val key = info.tags.firstOrNull { it.startsWith(REGION_TAG_PREFIX) }
            ?.removePrefix(REGION_TAG_PREFIX)
            ?: return

        when (info.state) {
            WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> {
                val percent = info.progress.getInt(RegionDownloadWorker.KEY_PROGRESS, 0)
                _downloadProgress.update { it + (key to percent) }
            }
            WorkInfo.State.SUCCEEDED -> if (processedTerminalIds.add(info.id)) {
                _completedRegions.update { it + key }
                _downloadProgress.update { it - key }
            }
            WorkInfo.State.FAILED -> if (processedTerminalIds.add(info.id)) {
                _downloadProgress.update { it - key }
                val message = info.outputData.getString(RegionDownloadWorker.KEY_ERROR)
                downloadNames[key]?.let { name ->
                    _events.tryEmit(RegionsEvent.DownloadFailed(key, name, message))
                }
            }
            WorkInfo.State.CANCELLED -> if (processedTerminalIds.add(info.id)) {
                _downloadProgress.update { it - key }
            }
        }
    }

    fun loadRegions() {
        viewModelScope.launch {
            getAllRegionsUseCase().fold(
                onSuccess = { regions -> _state.value = RegionsListUiState.Success(regions) },
                onFailure = { error -> _state.value = RegionsListUiState.Error(message = error.toString()) }
            )
        }
    }

    fun loadMemoryInfo() {
        viewModelScope.launch {
            _memoryInfo.value = getDeviceMemoryInfoUseCase()
        }
    }

    fun cancelDownload(regionKey: String) {
        downloadScheduler.cancel(regionKey)
    }

    fun startDownload(regionKey: String, downloadName: String) {
        downloadNames[regionKey] = downloadName
        _completedRegions.update { it - regionKey }
        _downloadProgress.update { it + (regionKey to 0) }
        viewModelScope.launch(Dispatchers.IO) {
            downloadScheduler.enqueue(regionKey, downloadName)
        }
    }

    private companion object {
        const val REGION_TAG_PREFIX = "region:"
    }
}
