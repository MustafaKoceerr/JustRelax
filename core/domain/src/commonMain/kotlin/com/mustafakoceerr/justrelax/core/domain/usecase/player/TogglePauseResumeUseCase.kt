package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer

/**
 * Ana oynatma durumunu (Play/Pause) tersine çevirir (toggle).
 * ViewModel'in durum kontrolü yapmasına gerek kalmaz; bu Use Case,
 * o anki duruma göre doğru eylemi (pauseAll veya resumeAll) kendisi seçer.
 */
class TogglePauseResumeUseCase(
    private val audioMixer: AudioMixer
) {
    /**
     * Mevcut çalma durumu `true` ise `pauseAll()` çağırır, `false` ise `resumeAll()` çağırır.
     * Bu bir suspend fonksiyonudur ve bir CoroutineScope içinden çağrılmalıdır.
     */
    suspend operator fun invoke() {
        // O anki durumu doğrudan AudioMixer'ın state'inden anlık olarak alıyoruz.
        val isCurrentlyPlaying = audioMixer.state.value.isPlaying

        if (isCurrentlyPlaying) {
            audioMixer.pauseAll()
        } else {
            audioMixer.resumeAll()
        }
    }
}