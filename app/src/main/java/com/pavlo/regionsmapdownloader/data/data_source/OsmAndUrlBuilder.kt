package com.pavlo.regionsmapdownloader.data.data_source

object OsmAndUrlBuilder {
    private const val BASE_URL = "https://download.osmand.net/download"
    private const val MAP_VERSION = "2"

    fun mapUrl(downloadName: String): String {
        val fileName = downloadName.replaceFirstChar { it.uppercase() } + "_${MAP_VERSION}.obf.zip"
        return "$BASE_URL?standard=yes&file=$fileName"
    }
}
