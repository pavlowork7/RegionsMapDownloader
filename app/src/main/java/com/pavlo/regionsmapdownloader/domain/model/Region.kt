package com.pavlo.regionsmapdownloader.domain.model

data class Region(
    val name: String,
    val isMap: Boolean,
    val subRegions: List<Region>,
    val hasChildren: Boolean
)