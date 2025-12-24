package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import kotlinx.coroutines.flow.Flow

/**
 * Sorumluluk: UI'ın hangi seslerin çaldığını bilmesini sağlamak.
 * ViewModel bu Flow'u collect edip State'ini güncelleyecek.
 */
class GetPlayingSoundsUseCase(
    private val audioMixer: AudioMixer
) {
    operator fun invoke(): Flow<Set<String>> {
        return audioMixer.playingSoundIds
    }
}