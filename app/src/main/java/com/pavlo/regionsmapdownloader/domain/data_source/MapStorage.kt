package com.pavlo.regionsmapdownloader.domain.data_source

import java.io.File

interface MapStorage {
    fun mapFile(downloadName: String): File

    /** Імена вже завантажених карт — джерело істини для позначки «завантажено». */
    fun downloadedNames(): Set<String>
}
