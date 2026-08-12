package com.pavlo.regionsmapdownloader.data.repository

import android.content.Context
import com.pavlo.regionsmapdownloader.data.model.XmlParser
import com.pavlo.regionsmapdownloader.data.model.toDomain
import com.pavlo.regionsmapdownloader.domain.model.Region
import com.pavlo.regionsmapdownloader.domain.repository.RegionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class RegionRepositoryImpl(private val context: Context) : RegionRepository {

    private val mutex = Mutex()
    private var cached: List<Region>? = null

    override suspend fun getRegions(): Result<List<Region>> = withContext(Dispatchers.IO) {
        runCatching {
            mutex.withLock {
                cached ?: XmlParser.parseItemsFromAssets(context, REGIONS_FILE)
                    .map { it.toDomain() }
                    .also { cached = it }
            }
        }
    }

    private companion object {
        const val REGIONS_FILE = "regions.xml"
    }
}
