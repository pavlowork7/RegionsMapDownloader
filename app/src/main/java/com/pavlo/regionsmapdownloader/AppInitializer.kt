package com.pavlo.regionsmapdownloader

import android.content.Context
import com.pavlo.regionsmapdownloader.data.repository.RegionRepositoryImpl
import com.pavlo.regionsmapdownloader.domain.repository.RegionRepository
import com.pavlo.regionsmapdownloader.domain.usecase.GetAllRegionsUseCase

class AppInitializer(context: Context) {
    private val appContext = context.applicationContext

    val regionRepository: RegionRepository by lazy {
        RegionRepositoryImpl(appContext)
    }

    val getAllRegionsUseCase: GetAllRegionsUseCase by lazy {
        GetAllRegionsUseCase(regionRepository)
    }
}