package com.pavlo.regionsmapdownloader.data.data_source

import com.pavlo.regionsmapdownloader.domain.data_source.RegionDownloadDataSource
import com.pavlo.regionsmapdownloader.domain.model.DownloadProgress
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipInputStream

class RegionDownloadDataSourceImpl(
    private val okHttpClient: OkHttpClient
): RegionDownloadDataSource {

    override fun downloadMap(url: String, destination: File): Flow<DownloadProgress> = flow {
        val request = Request.Builder().url(url).header("Accept-Encoding", "identity").build()

        val archiveFile = File(destination.parentFile, "${destination.name}.zip.tmp")
        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Download failed: ${response.code}")
                val body = response.body
                val totalBytes = body.contentLength()
                var downloadedBytes = 0L
                var lastReportedPercent = -1

                body.byteStream().use { input ->
                    FileOutputStream(archiveFile).use { output ->
                        val buffer = ByteArray(FILE_BUFFER_VALUE)
                        var bytes = input.read(buffer)
                        while (bytes >= 0) {
                            currentCoroutineContext().ensureActive()
                            output.write(buffer, 0, bytes)
                            downloadedBytes += bytes
                            val percent = if (totalBytes > 0) getPercentage(downloadedBytes, totalBytes).toInt() else 0
                            if (percent != lastReportedPercent) {
                                lastReportedPercent = percent
                                emit(DownloadProgress.InProgress(percent))
                            }
                            bytes = input.read(buffer)
                        }
                    }
                }
            }
            extractMapEntry(archiveFile, destination)
        } finally {
            archiveFile.delete()
        }

        emit(DownloadProgress.Completed(destination.path))
    }.flowOn(Dispatchers.IO)

    private fun extractMapEntry(archiveFile: File, destination: File) {
        val tempFile = File(destination.parentFile, "${destination.name}.part")
        try {
            ZipInputStream(FileInputStream(archiveFile).buffered()).use { zipStream ->
                generateSequence { zipStream.nextEntry }.firstOrNull {
                    !it.isDirectory && it.name.endsWith(MAP_ENTRY_EXTENSION, ignoreCase = true)
                } ?: throw IOException("Downloaded archive contains no ${MAP_ENTRY_EXTENSION} file")
                FileOutputStream(tempFile).use { output -> zipStream.copyTo(output) }
            }
            if (!tempFile.renameTo(destination)) throw IOException("Cannot finalize ${destination.name}")
        } finally {
            tempFile.delete()
        }
    }

    private fun getPercentage(part: Long, total: Long) = ((part * HUNDRED_PERCENT_VALUE) / total)

    companion object {
        private const val FILE_BUFFER_VALUE = 32 * 1024
        private const val HUNDRED_PERCENT_VALUE = 100
        private const val MAP_ENTRY_EXTENSION = ".obf"
    }
}
