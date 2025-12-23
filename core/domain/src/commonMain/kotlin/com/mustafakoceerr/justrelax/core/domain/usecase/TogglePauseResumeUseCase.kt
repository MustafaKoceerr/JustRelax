package com.mustafakoceerr.justrelax.core.domain.usecase

import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer

/**
 * Sorumluluk: Ana oynatma/duraklatma (Play/Pause) butonunun işlevini yönetmek.
 * Duruma göre (isPaused) karar verir.
 */
class TogglePauseResumeUseCase(
    private val audioMixer: AudioMixer
) {
    // UI'da "isPlaying" state'ini tutuyoruz.
    // Kullanıcı butona bastığında:
    // isPlaying == true -> PauseAll çağır
    // isPlaying == false -> ResumeAll çağır

    fun pauseAll() {
        audioMixer.pauseAll()
    }

    fun resumeAll() {
        audioMixer.resumeAll()
    }
}