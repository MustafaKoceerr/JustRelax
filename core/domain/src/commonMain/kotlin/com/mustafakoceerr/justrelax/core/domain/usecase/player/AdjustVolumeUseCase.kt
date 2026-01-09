package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer

class AdjustVolumeUseCase(
    private val audioMixer: AudioMixer
) {
    operator fun invoke(soundId: String, volume: Float) {
        val safeVolume = volume.coerceIn(0f, 1f)
        audioMixer.setVolume(soundId, safeVolume)
    }
}