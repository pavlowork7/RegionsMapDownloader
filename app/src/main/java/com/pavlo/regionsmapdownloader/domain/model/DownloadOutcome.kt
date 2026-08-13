package com.pavlo.regionsmapdownloader.domain.model

sealed interface DownloadOutcome {
    data object Success : DownloadOutcome
    data object Cancelled : DownloadOutcome
    data class Failure(val message: String?) : DownloadOutcome
}
