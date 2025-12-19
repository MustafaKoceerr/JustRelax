package com.mustafakoceerr.justrelax.feature.ai.domain.usecase

import com.mustafakoceerr.justrelax.core.audio.SoundManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * AI Prompt'u ve UI Context bilgisi için aktif seslerin listesini sağlar.
 * ViewModel'in SoundManager'ı bilmesini engeller.
 */
class ObserveActiveContextUseCase(
    private val soundManager: SoundManager
) {
    operator fun invoke(): Flow<List<ActiveSoundInfo>> {
        return soundManager.state
            .map { state -> state.activeSounds.values }
            .map { activeSounds ->
                activeSounds.map { activeSound ->
                    ActiveSoundInfo(
                        id = activeSound.sound.id,
                        name = activeSound.sound.name,
                        volume = activeSound.currentVolume
                    )
                }
            }
    }
}