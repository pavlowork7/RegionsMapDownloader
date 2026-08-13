package com.pavlo.regionsmapdownloader.domain.model

sealed class DownloadProgress {
    data class InProgress(val percent: Int) : DownloadProgress()
    data class Completed(val filePath: String) : DownloadProgress()
}