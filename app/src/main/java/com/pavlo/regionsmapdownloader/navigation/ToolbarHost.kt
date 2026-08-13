package com.pavlo.regionsmapdownloader.navigation

data class ToolbarState(val title: String, val showBackButton: Boolean)

interface ToolbarHost {
    fun configureToolbar(state: ToolbarState)
}
