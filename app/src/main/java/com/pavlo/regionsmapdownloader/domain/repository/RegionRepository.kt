package com.pavlo.regionsmapdownloader.domain.repository

import com.pavlo.regionsmapdownloader.domain.model.Region

interface RegionRepository {
    suspend fun getRegions(): Result<List<Region>>
}