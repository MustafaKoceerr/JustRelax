package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer

/**
 * Sorumluluk: Belirli bir sesi durdurmak.
 * Bu UseCase çok basittir, iş mantığı (Business Logic) gerektirmez
 * ama UI'ın doğrudan AudioMixer'a erişmesini engellemek için "Proxy" görevi görür.
 */
class StopSoundUseCase(
    private val audioMixer: AudioMixer
) {
    operator fun invoke(soundId: String) {
        // Burada ileride loglama veya analytics eklenebilir.
        // Örn: Analytics.logEvent("sound_stopped", soundId)
        audioMixer.stopSound(soundId)
    }
}