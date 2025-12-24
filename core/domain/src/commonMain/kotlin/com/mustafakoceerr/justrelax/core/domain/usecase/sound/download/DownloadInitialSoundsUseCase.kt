package com.mustafakoceerr.justrelax.core.domain.usecase.sound.download

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.SyncSoundsUseCase
import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow

/**
 * Sadece başlangıç paketi olarak işaretlenmiş sesleri indirir.
 */
/**
 * Sadece başlangıç paketi olarak işaretlenmiş sesleri indirir.
 */
class DownloadInitialSoundsUseCase(
    private val syncSoundsUseCase: SyncSoundsUseCase, // <--- YENİ BAĞIMLILIK
    private val soundRepository: SoundRepository,
    private val downloadSoundUseCase: DownloadSoundUseCase
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<DownloadStatus> = flow {
        // 1. ÖNCE SENKRONİZE ET
        val syncResult = syncSoundsUseCase()
        if (syncResult is Resource.Error) {
            emit(DownloadStatus.Error(syncResult.error.message))
            return@flow
        }

        // 2. ŞİMDİ VERİTABANINDAN OKU (Artık güncel olduğundan eminiz)
        val allSounds = soundRepository.getSounds().first()
        val initialSoundsToDownload = allSounds.filter { it.isInitial && !it.isDownloaded }

        if (initialSoundsToDownload.isEmpty()) {
            emit(DownloadStatus.Completed)
            return@flow
        }

        // 3. İNDİRMEYİ BAŞLAT (Bu kısım aynı)
        val flows = initialSoundsToDownload.map { downloadSoundUseCase(it.id) }
        flows.asFlow().flatMapMerge { it }.collect { status ->
            emit(status)
        }
    }
}