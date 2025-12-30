package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer

/**
 * Belirli bir sesin seviyesini ayarlar.
 * Bu bir 'fire-and-forget' işlemidir, anlık tepki verir ve suspend değildir.
 * Slider gibi hızlı güncellenen UI bileşenleri için idealdir.
 */
class AdjustVolumeUseCase(
    private val audioMixer: AudioMixer
) {
    /**
     * @param soundId Sesin kimliği.
     * @param volume Ayarlanacak yeni ses seviyesi (0.0f ile 1.0f arasında).
     */
    operator fun invoke(soundId: String, volume: Float) {
        // Güvenlik katmanı: UI'dan gelebilecek geçersiz değerleri (örn: -0.1f veya 1.1f)
        // engeller ve ses motoruna daima 0.0f-1.0f aralığında bir değer gönderir.
        val safeVolume = volume.coerceIn(0f, 1f)
        audioMixer.setVolume(soundId, safeVolume)
    }
}