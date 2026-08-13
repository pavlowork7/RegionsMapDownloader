package com.pavlo.regionsmapdownloader.ui

import com.pavlo.regionsmapdownloader.domain.model.Region

sealed class RegionListItem {
    data class ContinentHeader(val name: String) : RegionListItem()

    data class RegionRow(
        val region: Region,
        val path: List<String>,
        val progress: Int? = null,
        val isCompleted: Boolean = false
    ) : RegionListItem() {
        val rowKey: String = path.joinToString("/")
    }
}

fun List<Region>.toListItems(
    progress: Map<String, Int> = emptyMap(),
    completed: Set<String> = emptySet()
): List<RegionListItem> = flatMap { continent ->
    listOf(RegionListItem.ContinentHeader(continent.displayName)) +
        continent.subRegions.toRegionRows(listOf(continent.name), progress, completed)
}

fun List<Region>.toRegionRows(
    parentPath: List<String>,
    progress: Map<String, Int> = emptyMap(),
    completed: Set<String> = emptySet()
): List<RegionListItem.RegionRow> = map { it.toRow(parentPath + it.name, progress, completed) }

private fun Region.toRow(
    path: List<String>,
    progress: Map<String, Int>,
    completed: Set<String>
): RegionListItem.RegionRow = RegionListItem.RegionRow(
    region = this,
    path = path,
    progress = progress[downloadName],
    isCompleted = downloadName in completed
)

fun List<Region>.findByPath(path: List<String>): Region? {
    if (path.isEmpty()) return null
    val current = firstOrNull { it.name == path.first() } ?: return null
    return current.subRegions.findByPath(path.drop(1)) ?: current
}
