package com.mustafakoceerr.justrelax.core.domain.usecase.sound.download

import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

/**
 * Bu dosya, indirme UseCase'leri için yardımcı bir mantık içerir.
 * Model katmanından bağımsızdır, Domain katmanının (Business Logic) bir parçasıdır.
 */

internal fun List<Flow<DownloadStatus>>.combineToGlobalStatus(): Flow<DownloadStatus> {
    if (this.isEmpty()) {
        return flowOf(DownloadStatus.Completed)
    }

    return combine(this) { statuses ->
        // 1. Durum Analizi
        val completedCount = statuses.count { it is DownloadStatus.Completed }
        val errorCount = statuses.count { it is DownloadStatus.Error }
        val totalCount = statuses.size

        // 2. Bitiş Kontrolü
        // Hata alanları da "bitti" sayıyoruz (Fail Gracefully)
        if (completedCount + errorCount == totalCount) {
            return@combine DownloadStatus.Completed
        }

        // 3. İlerleme Hesaplama
        val totalProgress = statuses.sumOf { status ->
            when (status) {
                is DownloadStatus.Completed -> 1.0
                is DownloadStatus.Error -> 1.0 // Hata alan süreci tıkamasın
                is DownloadStatus.Progress -> status.progress.toDouble()
                else -> 0.0
            }
        }

        val averageProgress = (totalProgress / totalCount).toFloat()

        DownloadStatus.Progress(averageProgress)
    }
}