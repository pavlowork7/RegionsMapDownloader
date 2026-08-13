package com.pavlo.regionsmapdownloader.ui.utils

import androidx.fragment.app.Fragment

inline fun <reified T : Any> Fragment.requireHost(): T =
    parentFragment as? T
        ?: activity as? T
        ?: error(
            "${this::class.simpleName} requires a host implementing ${T::class.simpleName}, " +
                "but ${activity?.let { it::class.simpleName } ?: "no activity"} does not"
        )
