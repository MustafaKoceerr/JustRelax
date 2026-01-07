package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer

class StopAllSoundsUseCase(
    private val audioMixer: AudioMixer
) {
    suspend operator fun invoke() {
        audioMixer.stopAll()
    }
}