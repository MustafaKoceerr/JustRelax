package com.mustafakoceerr.justrelax.core.model

sealed interface DownloadStatus {
    data object Idle : DownloadStatus             // Henüz başlamadı
    data object Queued : DownloadStatus           // Sırada bekliyor
    data class Progress(val percentage: Float) : DownloadStatus // 0.0f - 1.0f arası ilerleme
    data object Completed : DownloadStatus        // Başarıyla bitti
    data class Error(val message: String) : DownloadStatus // Hata oluştu
}