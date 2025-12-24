package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import kotlinx.coroutines.flow.Flow

/**
 * Sorumluluk: Genel oynatma durumunu (Play/Pause) gözlemlemek.
 * UI bu akışı dinleyerek Play/Pause ikonunu günceller.
 */
class ObservePlaybackStateUseCase(
    private val audioMixer: AudioMixer
) {
    operator fun invoke(): Flow<Boolean> = audioMixer.isPlaying
}