package com.pavlo.regionsmapdownloader.data.data_source

import android.content.Context
import com.pavlo.regionsmapdownloader.domain.data_source.MapStorage
import java.io.File

class MapStorageImpl(private val context: Context) : MapStorage {

    override fun mapFile(downloadName: String): File = File(mapsDir(), "$downloadName$MAP_EXTENSION")

    override fun downloadedNames(): Set<String> =
        mapsDir().listFiles()
            ?.asSequence()
            ?.filter { it.isFile && it.name.endsWith(MAP_EXTENSION) }
            ?.map { it.name.removeSuffix(MAP_EXTENSION) }
            ?.toSet()
            .orEmpty()

    private fun mapsDir(): File {
        val dir = context.getExternalFilesDir(MAPS_DIR) ?: File(context.filesDir, MAPS_DIR)
        dir.mkdirs()
        return dir
    }

    private companion object {
        const val MAPS_DIR = "osmand"
        const val MAP_EXTENSION = ".obf"
    }
}
