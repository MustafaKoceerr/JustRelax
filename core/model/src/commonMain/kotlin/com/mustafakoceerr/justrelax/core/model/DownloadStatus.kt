package com.mustafakoceerr.justrelax.core.model

sealed interface DownloadStatus {
    data object Idle : DownloadStatus       // Hiç başlamadı
    data object Queued : DownloadStatus     // Listeye alındı, sırasını bekliyor
    data class Progress(val percentage: Float) : DownloadStatus // 0.0 - 1.0 arası
    data class Error(val message: String) : DownloadStatus
    data object Completed : DownloadStatus
}