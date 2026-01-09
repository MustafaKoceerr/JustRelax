package com.mustafakoceerr.justrelax.core.model

sealed interface DownloadStatus {
    data object Idle : DownloadStatus
    data object Queued : DownloadStatus
    data class Progress(val percentage: Float) : DownloadStatus
    data class Error(val message: String) : DownloadStatus
    data object Completed : DownloadStatus
}