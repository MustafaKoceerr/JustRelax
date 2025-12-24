package com.mustafakoceerr.justrelax.core.model

sealed interface DownloadStatus {
    data object Queued : DownloadStatus
    data class Progress(val progress: Float) : DownloadStatus
    data class Error(val message: String) : DownloadStatus
    data object Completed : DownloadStatus // Sadece sinyal, veri yok
}