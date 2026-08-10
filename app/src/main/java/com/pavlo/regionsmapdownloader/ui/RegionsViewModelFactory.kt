package com.pavlo.regionsmapdownloader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pavlo.regionsmapdownloader.domain.usecase.GetAllRegionsUseCase

class RegionsViewModelFactory(
    private val getAllRegionsUseCase: GetAllRegionsUseCase
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegionsViewModel(getAllRegionsUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}