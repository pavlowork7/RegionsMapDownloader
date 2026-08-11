package com.pavlo.regionsmapdownloader.data.data_source

import com.pavlo.regionsmapdownloader.domain.data_source.RegionDownloadDataSource
import com.pavlo.regionsmapdownloader.domain.model.DownloadProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) throw IOException("Download failed: ${response.code}")
        val body = response.body
        val totalBytes = body.contentLength()
        var downloadedBytes = 0L

        val archiveFile = File(destination.parentFile, "${destination.name}.zip.tmp")
        try {
            body.byteStream().use { input ->
                FileOutputStream(archiveFile).use { output ->
                    val buffer = ByteArray(FILE_BUFFER_VALUE)
                    var bytes = input.read(buffer)
                    while (bytes >= 0) {
                        output.write(buffer, 0, bytes)
                        downloadedBytes += bytes
                        val percent = if (totalBytes > 0) getPercentage(downloadedBytes, totalBytes).toInt() else 0
                        emit(DownloadProgress.InProgress(percent))
                        bytes = input.read(buffer)
                    }
                }
            }
            extractMapEntry(archiveFile, destination)
        } finally {
            archiveFile.delete()
        }

        emit(DownloadProgress.Completed(destination.path))
    }

    private fun extractMapEntry(archiveFile: File, destination: File) {
        ZipInputStream(FileInputStream(archiveFile)).use { zipStream ->
            val entry = generateSequence { zipStream.nextEntry }.firstOrNull { !it.isDirectory }
                ?: throw IOException("Downloaded archive contains no files")
            FileOutputStream(destination).use { output -> zipStream.copyTo(output) }
        }
    }

    private fun getPercentage(part: Long, total: Long) = ((part * HUNDRED_PERCENT_VALUE) / total)

    companion object {
        private const val FILE_BUFFER_VALUE = 8 * 1024
        private const val HUNDRED_PERCENT_VALUE = 100
    }
}