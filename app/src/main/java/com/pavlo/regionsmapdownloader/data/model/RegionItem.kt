package com.pavlo.regionsmapdownloader.data.model

import com.pavlo.regionsmapdownloader.domain.model.Region

data class RegionItem(
    val name: String? = null,
    val map: Boolean? = null,
    val regions: MutableList<RegionItem>? = null
)

fun RegionItem.toDomain(): Region {
    val children = regions.orEmpty()
    return Region(
        name = name ?: "",
        isMap = map ?: false,
        hasChildren = children.isNotEmpty(),
        subRegions = children.map { it.toDomain() }
    )
}