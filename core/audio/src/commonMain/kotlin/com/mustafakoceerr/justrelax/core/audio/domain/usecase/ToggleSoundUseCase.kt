package com.mustafakoceerr.justrelax.core.audio.domain.usecase

import com.mustafakoceerr.justrelax.core.audio.SoundManager
import com.mustafakoceerr.justrelax.core.domain.manager.SoundDownloader
import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ToggleSoundUseCase (
    private val soundManager: SoundManager,
    private val soundDownloader: SoundDownloader
){
    // Nested Interface: Sektörel standarttır, temizdir.
    // Neden İyi? "Namespace Pollution" (İsim Uzayı Kirliliği) engeller.
    sealed interface Result {
        data object Ignored : Result
        data object Toggled : Result
        data class Downloading(val isDownloading: Boolean) : Result
        data class Error(val message: String) : Result
    }


    operator fun invoke(sound: Sound, isCurrentlyDownloading: Boolean): Flow<Result> = flow {
        // 1. Zaten çalıyorsa durdur
        if (soundManager.state.value.activeSounds.containsKey(sound.id)) {
            soundManager.toggleSound(sound)
            emit(Result.Toggled)
            return@flow
        }

        // 2. Zaten indiriliyorsa işlem yapma
        if (isCurrentlyDownloading) {
            emit(Result.Ignored)
            return@flow
        }

        // 3. İndirilmişse çal
        if (sound.isDownloaded) {
            soundManager.toggleSound(sound)
            emit(Result.Toggled)
            return@flow
        }

        // 4. İndirilmemişse -> İNDİR
        emit(Result.Downloading(true))
        val isSuccess = soundDownloader.downloadSound(sound.id)
        emit(Result.Downloading(false))

        if (isSuccess) {
            // İndirme başarılı, ama sound objesi eski (isDownloaded=false).
            // Kullanıcı tekrar basmalı veya buradan otomatik tetiklemelisin.
            // Şimdilik kullanıcıya bırakıyoruz.
        } else {
            emit(Result.Error("Download failed"))
        }
    }
}