package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import com.mustafakoceerr.justrelax.core.domain.player.SoundConfig
import com.mustafakoceerr.justrelax.core.model.Sound

/**
 * Sorumluluk (SRP):
 * Verilen bir mix haritasını (Sound -> Volume) alıp,
 * bunu AudioMixer'ın anlayacağı SoundConfig formatına çevirerek
 * yeni mix'i çaldırmak.
 * Bu fonksiyon "fire-and-forget" çalışır, işlemin bitmesini beklemez.
 */
class SetMixUseCase(
    private val audioMixer: AudioMixer
) {
    // 'suspend' anahtar kelimesi kaldırıldı.
    operator fun invoke(mix: Map<Sound, Float>) {
        // 1. Map<Sound, Float> -> Map<String, SoundConfig> dönüşümü
        val mixConfigs = mix.map { (sound, volume) ->
            sound.id to SoundConfig(
                id = sound.id,
                url = sound.localPath ?: "",
                initialVolume = volume,
                // Mix geçişlerinde ani bir başlangıç için fade-in'i 0 yapıyoruz.
                fadeInDurationMs = 0L
            )
        }.toMap()

        // 2. Hazırlanan konfigürasyonu Mixer'a gönder.
        // Bu çağrı artık anında geri döner, bekleme yapmaz.
        audioMixer.setMix(mixConfigs)
    }
}