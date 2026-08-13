package com.pavlo.regionsmapdownloader.ui.utils

import java.util.Locale
import kotlin.math.pow

object StringFormatHelper {

    private const val BYTES_PER_GIB = 1024.0

    fun formatGb(bytes: Long, format: String): String =
        String.format(Locale.getDefault(), format, bytes / BYTES_PER_GIB.pow(3))
}