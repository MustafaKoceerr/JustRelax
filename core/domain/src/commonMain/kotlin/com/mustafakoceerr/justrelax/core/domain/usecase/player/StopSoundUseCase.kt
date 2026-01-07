package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer

class StopSoundUseCase(
    private val audioMixer: AudioMixer
) {
    suspend operator fun invoke(soundId: String) {
        audioMixer.stopSound(soundId)
    }
}