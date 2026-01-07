package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer

class TogglePauseResumeUseCase(
    private val audioMixer: AudioMixer
) {
    suspend operator fun invoke() {
        val isCurrentlyPlaying = audioMixer.state.value.isPlaying
        if (isCurrentlyPlaying) {
            audioMixer.pauseAll()
        } else {
            audioMixer.resumeAll()
        }
    }
}