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

class DownloadInitialSoundsUseCase(
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

        // 2. Verileri çek ve filtrele (Sadece Initial olanlar)
        val allSounds = soundRepository.getSounds().first()
        val initialSoundsToDownload = allSounds.filter { it.isInitial && !it.isDownloaded }

        if (initialSoundsToDownload.isEmpty()) {
            emit(DownloadStatus.Completed)
            return@flow
        }

        // 3. Akışları oluştur
        val downloadFlows = initialSoundsToDownload.map { sound ->
            downloadSoundUseCase(sound.id)
        }

        // 4. Birleştir ve yay
        emitAll(downloadFlows.combineToGlobalStatus())
    }
}