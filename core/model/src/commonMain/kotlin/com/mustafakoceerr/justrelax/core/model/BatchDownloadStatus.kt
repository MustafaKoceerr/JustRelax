package com.mustafakoceerr.justrelax.core.model

sealed interface BatchDownloadStatus{
    // İlerleme yüzdesi (0.0 - 1.0)
    data class Progress(val percentage: Float): BatchDownloadStatus

    // İşlem bitti
    data object Completed: BatchDownloadStatus

    // Hata (Opsiyonel, şu an kullanmasak bile yapıda bulunsun)
    data class Error(val message: String): BatchDownloadStatus
}