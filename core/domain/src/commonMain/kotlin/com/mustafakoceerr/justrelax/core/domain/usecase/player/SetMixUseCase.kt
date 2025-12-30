package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import com.mustafakoceerr.justrelax.core.domain.player.SoundConfig
import com.mustafakoceerr.justrelax.core.model.Sound

/**
 * Belirtilen bir ses karışımını (mix) çalar.
 * Önceki tüm sesleri durdurur ve listedeki sesleri çalmaya başlar.
 */
class SetMixUseCase(
    private val audioMixer: AudioMixer
) {
    /**
     * @param mix Çalınacak sesleri ve onların ses seviyelerini içeren bir harita.
     * @param useFadeIn Sesler çalmaya başlarken yumuşak bir giriş (fade-in) kullanılsın mı?
     */
    suspend operator fun invoke(
        mix: Map<Sound, Float>,
        useFadeIn: Boolean = true // Varsayılan olarak yumuşak geçiş yap
    ) {
        // 1. GÜVENLİK & DÖNÜŞÜM:
        // Verilen mix'i, AudioMixer'ın anlayacağı List<SoundConfig>'e çeviriyoruz.
        // Bu sırada, dosyası indirilmemiş (localPath'i boş olan) sesleri atlıyoruz.
        val configs = mix.mapNotNull { (sound, volume) ->
            val path = sound.localPath
            if (path.isNullOrBlank()) {
                null // Geçersiz sesi filtrele ve atla
            } else {
                SoundConfig(
                    id = sound.id,
                    url = path,
                    initialVolume = volume.coerceIn(0f, 1f),
                    fadeInDurationMs = if (useFadeIn) AudioDefaults.FADE_IN_MS else 0L
                )
            }
        }

        // 2. AKSİYON: Hazırlanan ve temizlenen listeyi Mixer'a gönder.
        audioMixer.setMix(configs)
    }
}