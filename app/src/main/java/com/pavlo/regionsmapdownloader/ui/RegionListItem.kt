package com.pavlo.regionsmapdownloader.ui

import com.pavlo.regionsmapdownloader.domain.model.Region

sealed class RegionListItem {
    data class ContinentHeader(val name: String) : RegionListItem()
    data class RegionRow(val region: Region, val path: List<String>): RegionListItem() {

        val downloadUrl = "https://download.osmand.net/download?standard=yes&file=${getFileName()}_2.obf.zip"

        fun getFileName(): String {
            val components = path.toMutableList()
            val continent = components.removeAt(0)
            components.add(continent)
            val uppercasedRegion = components[0].replaceFirstChar(Char::uppercase)
            components.removeAt(0)
            components.add(0, uppercasedRegion)
            return components.joinToString("_")
        }
    }
}

fun List<Region>.toListItems(): List<RegionListItem> = flatMap { continent ->
    listOf(RegionListItem.ContinentHeader(continent.name)) +
        continent.subRegions.map { RegionListItem.RegionRow(it, listOf(continent.name, it.name)) }
}

fun List<Region>.findByPath(path: List<String>): Region? {
    if (path.isEmpty()) return null
    val current = firstOrNull { it.name == path.first() } ?: return null
    return current.subRegions.findByPath(path.drop(1)) ?: current
}
