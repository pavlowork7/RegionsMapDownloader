package com.pavlo.regionsmapdownloader.data.repository

import android.content.Context
import com.pavlo.regionsmapdownloader.data.model.XmlParser
import com.pavlo.regionsmapdownloader.data.model.toDomain
import com.pavlo.regionsmapdownloader.domain.model.Region
import com.pavlo.regionsmapdownloader.domain.repository.RegionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegionRepositoryImpl(private val context: Context): RegionRepository {

    override suspend fun getRegions(): Result<List<Region>> = withContext(Dispatchers.IO) {
        try {
            val parsedData = XmlParser.parseItemsFromAssets(context, "regions.xml").map { it.toDomain() }
            Result.success(parsedData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}