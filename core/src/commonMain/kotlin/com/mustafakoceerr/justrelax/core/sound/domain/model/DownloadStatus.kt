package com.mustafakoceerr.justrelax.core.sound.domain.model

sealed interface DownloadStatus{
    data object Idle: DownloadStatus
    data object Started: DownloadStatus
    data class Progress(val progress: Float): DownloadStatus
    data object Completed: DownloadStatus
    data class Error(val message: String): DownloadStatus
}