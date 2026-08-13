package com.pavlo.regionsmapdownloader.navigation

interface Navigator {
    fun openRegionList(path: List<String> = emptyList())
}

interface NavigatorProvider {
    val navigator: Navigator
}
