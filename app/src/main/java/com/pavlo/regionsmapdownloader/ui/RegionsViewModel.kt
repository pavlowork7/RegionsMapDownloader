package com.pavlo.regionsmapdownloader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavlo.regionsmapdownloader.domain.model.Region
import com.pavlo.regionsmapdownloader.domain.usecase.GetAllRegionsUseCase
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
    private val getAllRegionsUseCase: GetAllRegionsUseCase
): ViewModel() {
    private val _state = MutableStateFlow<RegionsListUiState>(RegionsListUiState.Loading)
    val state: StateFlow<RegionsListUiState> = _state.asStateFlow()

    fun loadContent() {
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
}