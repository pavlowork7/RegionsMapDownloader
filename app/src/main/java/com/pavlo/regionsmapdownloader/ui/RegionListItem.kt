package com.pavlo.regionsmapdownloader.ui

import com.pavlo.regionsmapdownloader.domain.model.Region

sealed class RegionListItem {
    data class ContinentHeader(val name: String) : RegionListItem()
    data class RegionRow(val region: Region) : RegionListItem()
}

fun List<Region>.toListItems(): List<RegionListItem> = flatMap { continent ->
    listOf(RegionListItem.ContinentHeader(continent.name)) +
        continent.subRegions.map { RegionListItem.RegionRow(it) }
}
