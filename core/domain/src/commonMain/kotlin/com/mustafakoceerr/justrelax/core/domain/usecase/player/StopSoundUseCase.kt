package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer

/**
 * Belirtilen tek bir sesi durdurur.
 */
class StopSoundUseCase(
    private val audioMixer: AudioMixer
) {
    /**
     * @param soundId Durdurulacak sesin kimliği.
     * Bu bir suspend fonksiyonudur ve bir CoroutineScope içinden çağrılmalıdır.
     */
    suspend operator fun invoke(soundId: String) {
        // Gelecekte buraya loglama veya analytics eklenebilir.
        // Örn: Analytics.logEvent("sound_stopped", soundId)
        audioMixer.stopSound(soundId)
    }
}