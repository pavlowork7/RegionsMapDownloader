package com.pavlo.regionsmapdownloader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavlo.regionsmapdownloader.data.queue.DownloadQueue
import com.pavlo.regionsmapdownloader.data.worker.RegionDownloadScheduler
import com.pavlo.regionsmapdownloader.domain.model.DeviceMemoryInfo
import com.pavlo.regionsmapdownloader.domain.model.DownloadQueueEvent
import com.pavlo.regionsmapdownloader.domain.model.DownloadQueueItem
import com.pavlo.regionsmapdownloader.domain.model.Region
import com.pavlo.regionsmapdownloader.domain.usecase.GetAllRegionsUseCase
import com.pavlo.regionsmapdownloader.domain.usecase.GetDeviceMemoryInfoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class RegionsListUiState {
    data object Loading : RegionsListUiState()
    data class Success(val regions: List<Region>) : RegionsListUiState()
    data class Error(val message: String) : RegionsListUiState()
}

class RegionsViewModel(
    private val getAllRegionsUseCase: GetAllRegionsUseCase,
    private val getDeviceMemoryInfoUseCase: GetDeviceMemoryInfoUseCase,
    private val downloadScheduler: RegionDownloadScheduler,
    private val downloadQueue: DownloadQueue
) : ViewModel() {

    private val _state = MutableStateFlow<RegionsListUiState>(RegionsListUiState.Loading)
    val state: StateFlow<RegionsListUiState> = _state.asStateFlow()

    private val _memoryInfo = MutableStateFlow<DeviceMemoryInfo?>(null)
    val memoryInfo: StateFlow<DeviceMemoryInfo?> = _memoryInfo.asStateFlow()

    /** Прогрес за іменем завантаження: активна карта — реальний відсоток, ті, що в черзі — 0. */
    val downloadProgress: StateFlow<Map<String, Int>> = downloadQueue.state
        .map { it.progressByName }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    /** Завантажені карти визначаються за файлами на диску, тож переживають перезапуск застосунку. */
    val completedRegions: StateFlow<Set<String>> = downloadQueue.completed

    val events: SharedFlow<DownloadQueueEvent> = downloadQueue.events

    init {
        viewModelScope.launch { downloadQueue.restore() }
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

    fun startDownload(downloadName: String, displayName: String) {
        viewModelScope.launch {
            downloadScheduler.enqueue(DownloadQueueItem(downloadName, displayName))
        }
    }

    fun cancelDownload(downloadName: String) {
        viewModelScope.launch {
            downloadScheduler.cancel(downloadName)
        }
    }
}
