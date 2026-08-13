package com.pavlo.regionsmapdownloader.domain.data_source

import java.io.File

interface MapStorage {
    fun mapFile(downloadName: String): File
    fun downloadedNames(): Set<String>
}
