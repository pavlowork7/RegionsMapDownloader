package com.pavlo.regionsmapdownloader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pavlo.regionsmapdownloader.domain.usecase.GetAllRegionsUseCase
import com.pavlo.regionsmapdownloader.domain.usecase.GetDeviceMemoryInfoUseCase

class RegionsViewModelFactory(
    private val getAllRegionsUseCase: GetAllRegionsUseCase,
    private val getDeviceMemoryInfoUseCase: GetDeviceMemoryInfoUseCase
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegionsViewModel(getAllRegionsUseCase, getDeviceMemoryInfoUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}