package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer

class PauseAllSoundsUseCase(private val audioMixer: AudioMixer) {
    operator fun invoke() = audioMixer.pauseAll()
}