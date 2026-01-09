package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import com.mustafakoceerr.justrelax.core.domain.player.GlobalMixerState
import kotlinx.coroutines.flow.StateFlow

class GetGlobalMixerStateUseCase(
    private val audioMixer: AudioMixer
) {
    operator fun invoke(): StateFlow<GlobalMixerState> {
        return audioMixer.state
    }
}