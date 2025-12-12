package com.mustafakoceerr.justrelax.core.audio.domain.usecase

import com.mustafakoceerr.justrelax.core.audio.SoundManager
import com.mustafakoceerr.justrelax.core.domain.manager.SoundDownloader
import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ToggleSoundUseCase(
    private val soundManager: SoundManager,
    private val soundDownloader: SoundDownloader
) {
    sealed interface Result {
        data object Ignored : Result // İşlem yapılmadı (Zaten iniyor vb.)
        data object Toggled : Result // Açıldı veya Kapandı (Service tetiklenir)
        data class Downloading(val isDownloading: Boolean) : Result // İndirme durumu
        data class Error(val message: String) : Result
    }

    operator fun invoke(sound: Sound, isCurrentlyDownloading: Boolean): Flow<Result> = flow {
        // 1. DURUM: Ses zaten çalıyor mu? -> DURDUR
        // SoundManager state'i günceller -> Service bunu görür -> Notification güncellenir.
        if (soundManager.state.value.activeSounds.containsKey(sound.id)) {
            soundManager.toggleSound(sound)
            emit(Result.Toggled)
            return@flow
        }

        // 2. DURUM: Zaten şu an indiriliyor mu? -> İŞLEM YAPMA
        if (isCurrentlyDownloading) {
            emit(Result.Ignored)
            return@flow
        }

        // 3. DURUM: Dosya cihazda var mı? -> ÇAL
        // SoundManager state'i günceller -> Service başlar -> Ses çalar.
        if (sound.isDownloaded) {
            soundManager.toggleSound(sound)
            emit(Result.Toggled)
            return@flow
        }

        // 4. DURUM: Dosya yok -> SADECE İNDİR
        emit(Result.Downloading(true))

        // İndirme işlemini başlat
        val isSuccess = soundDownloader.downloadSound(sound.id)

        emit(Result.Downloading(false))

        if (isSuccess) {
            // İndirme başarılı.
            // BURADA DURUYORUZ. Otomatik çalma yok.
            // Kullanıcı UI'da "İndirildi" ikonunu görecek (Repository flow'u sayesinde)
            // ve isterse tekrar basacak.
        } else {
            emit(Result.Error("İndirme başarısız."))
        }
    }
}