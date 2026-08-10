package com.pavlo.regionsmapdownloader.ui.utils

import java.util.Locale

object StringFormatHelper {

    private const val ONE_BILLION_VALUE = 1_000_000_000.0

    fun formatGb(bytes: Long): String =
        String.format(Locale.getDefault(), "%.2f Gb", bytes / ONE_BILLION_VALUE)
}