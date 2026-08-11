package com.pavlo.regionsmapdownloader.ui

import com.pavlo.regionsmapdownloader.domain.model.Region

sealed class RegionListItem {
    data class ContinentHeader(val name: String) : RegionListItem()
    data class RegionRow(val region: Region, val path: List<String>): RegionListItem() {

        val downloadKey: String = path.joinToString("/")

        val downloadUrl = "https://download.osmand.net/download?standard=yes&file=${region.downloadName.replaceFirstChar { it.uppercase() }}_2.obf.zip"
    }
}

fun List<Region>.toListItems(): List<RegionListItem> = flatMap { continent ->
    listOf(RegionListItem.ContinentHeader(continent.displayName)) +
        continent.subRegions.map { RegionListItem.RegionRow(it, listOf(continent.name, it.name)) }
}

fun List<Region>.findByPath(path: List<String>): Region? {
    if (path.isEmpty()) return null
    val current = firstOrNull { it.name == path.first() } ?: return null
    return current.subRegions.findByPath(path.drop(1)) ?: current
}
