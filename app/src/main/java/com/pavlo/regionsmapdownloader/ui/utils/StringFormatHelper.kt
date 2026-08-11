package com.pavlo.regionsmapdownloader.ui.utils

import java.util.Locale

object StringFormatHelper {

    private const val ONE_BILLION_VALUE = 1_000_000_000.0

    fun formatGb(bytes: Long, format: String): String =
        String.format(Locale.getDefault(), format, bytes / ONE_BILLION_VALUE)
}