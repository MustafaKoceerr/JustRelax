package com.mustafakoceerr.justrelax.core.domain.usecase.sound.download

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.SyncSoundsUseCase
import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class DownloadAllSoundsUseCase(
    private val syncSoundsUseCase: SyncSoundsUseCase,
    private val soundRepository: SoundRepository,
    private val downloadSoundUseCase: DownloadSoundUseCase
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<DownloadStatus> = flow {
        // 1. Sync
        val syncResult = syncSoundsUseCase()
        if (syncResult is Resource.Error) {
            emit(DownloadStatus.Error(syncResult.error.message))
            return@flow
        }

        // 2. Veritabanından verileri çek
        val allSounds = soundRepository.getSounds().first()
        val soundsToDownload = allSounds.filter { !it.isDownloaded }

        if (soundsToDownload.isEmpty()) {
            emit(DownloadStatus.Completed)
            return@flow
        }

        // 3. Her ses için bir indirme akışı oluştur
        val downloadFlows = soundsToDownload.map { sound ->
            downloadSoundUseCase(sound.id)
        }

        // 4. Akışları birleştir ve genel durumu yay (YENİ MANTIK)
        // Artık tek tek emit etmiyoruz, birleştirilmiş sonucu emit ediyoruz.
        emitAll(downloadFlows.combineToGlobalStatus())
    }
}