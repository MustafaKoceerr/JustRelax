package com.mustafakoceerr.justrelax.feature.home.usecase

import com.mustafakoceerr.justrelax.core.domain.manager.SoundDownloader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class DownloadAllMissingSoundsUseCase (
    private val repository: SoundRepository,
    private val soundDownloader : SoundDownloader
){

    operator fun invoke(): Flow<BatchDownloadStatus> = flow {
        // 1. Veriyi Çek ve Filtrele
        val allSounds = repository.getSounds().first()
        val missingSounds = allSounds.filter { !it.isDownloaded }
        val totalMissing = missingSounds.size

        // Eğer eksik yoksa direkt bitir
        if (totalMissing == 0) {
            emit(BatchDownloadStatus.Completed)
            return@flow
        }

        // Başlangıç progress'i
        emit(BatchDownloadStatus.Progress(0f))

        var downloadedCount = 0

        // 2. indirme döngüsü
        missingSounds.forEach { sound ->
            // Tekil indirme fonksiyonunu çağırıyoruz (Boolean döner)
            // Not: Burada 'downloadSoundFlow' yerine 'downloadSound' (suspend) kullanmak
            // batch işlemleri için daha temizdir, ara detaylarla boğulmayız.
            val isSuccess = soundDownloader.downloadSound(sound.id)

            if (isSuccess){
                downloadedCount++
                val progress = downloadedCount.toFloat() / totalMissing.toFloat()
                emit(BatchDownloadStatus.Progress(progress))
            }
        }

        // 3. Bitiş
        emit(BatchDownloadStatus.Completed)
    }

}