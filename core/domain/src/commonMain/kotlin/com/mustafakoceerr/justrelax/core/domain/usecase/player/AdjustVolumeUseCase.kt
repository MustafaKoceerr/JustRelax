package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer

/**
 * Sorumluluk: Çalan bir sesin ses seviyesini değiştirmek.
 * Slider değişimlerinde anlık olarak çağrılır.
 */
class AdjustVolumeUseCase(
    private val audioMixer: AudioMixer
) {
    /**
     * @param volume: 0.0f ile 1.0f arasında olmalıdır.
     */
    operator fun invoke(soundId: String, volume: Float) {
        // UI'dan 0-100 arası geliyorsa burada 0.0-1.0'a çevirebiliriz.
        // Ama genelde Slider 0.0-1.0 verir.
        // Güvenlik kontrolü (Mixer da yapabilir ama UseCase de filtreleyebilir)
        val safeVolume = volume.coerceIn(0f, 1f)
        audioMixer.setVolume(soundId, safeVolume)
    }
}