package com.pavlo.regionsmapdownloader.domain.usecase

import com.pavlo.regionsmapdownloader.BuildConfig
import com.pavlo.regionsmapdownloader.domain.model.Region
import com.pavlo.regionsmapdownloader.domain.repository.RegionRepository

class GetAllRegionsUseCase(
    private val repository: RegionRepository
) {
    suspend operator fun invoke(): Result<List<Region>> {
        return repository.getRegions().map { regions ->
            if (BuildConfig.REGION_FILTER.isBlank()) regions
            else regions.filter { it.name == BuildConfig.REGION_FILTER }
        }
    }
}