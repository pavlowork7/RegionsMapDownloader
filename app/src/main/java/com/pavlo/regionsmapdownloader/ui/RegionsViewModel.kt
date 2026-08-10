package com.pavlo.regionsmapdownloader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavlo.regionsmapdownloader.domain.model.DeviceMemoryInfo
import com.pavlo.regionsmapdownloader.domain.model.Region
import com.pavlo.regionsmapdownloader.domain.usecase.GetAllRegionsUseCase
import com.pavlo.regionsmapdownloader.domain.usecase.GetDeviceMemoryInfoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RegionsListUiState {
    data object Loading: RegionsListUiState()
    data class Success(val regions: List<Region>): RegionsListUiState()
    data class Error(val message: String): RegionsListUiState()
}

class RegionsViewModel(
    private val getAllRegionsUseCase: GetAllRegionsUseCase,
    private val getDeviceMemoryInfoUseCase: GetDeviceMemoryInfoUseCase
): ViewModel() {
    private val _state = MutableStateFlow<RegionsListUiState>(RegionsListUiState.Loading)
    val state: StateFlow<RegionsListUiState> = _state.asStateFlow()

    private val _memoryInfo = MutableStateFlow<DeviceMemoryInfo?>(null)
    val memoryInfo: StateFlow<DeviceMemoryInfo?> = _memoryInfo.asStateFlow()

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
}