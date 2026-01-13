package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import com.mustafakoceerr.justrelax.core.domain.player.SoundConfig
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.SoundUi

class SetMixUseCase(
    private val audioMixer: AudioMixer
) {
    suspend operator fun invoke(
        mix: Map<SoundUi, Float>,
        useFadeIn: Boolean = true
    ) {
        val configs = mix.mapNotNull { (sound, volume) ->
            val path = sound.localPath

            if (path.isNullOrBlank()) {
                null
            } else {
                SoundConfig(
                    id = sound.id,
                    url = path,
                    initialVolume = volume.coerceIn(0f, 1f),
                    fadeInDurationMs = if (useFadeIn) AudioDefaults.FADE_IN_MS else 0L
                )
            }
        }
        audioMixer.setMix(configs)
    }
}