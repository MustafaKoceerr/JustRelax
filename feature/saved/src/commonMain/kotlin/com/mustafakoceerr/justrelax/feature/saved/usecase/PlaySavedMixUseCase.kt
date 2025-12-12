package com.mustafakoceerr.justrelax.feature.saved.usecase

import com.mustafakoceerr.justrelax.core.audio.SoundManager
import com.mustafakoceerr.justrelax.core.domain.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.model.SavedMix
import kotlinx.coroutines.flow.first


class PlaySavedMixUseCase(
    private val soundRepository: SoundRepository,
    private val soundManager: SoundManager
) {
    suspend operator fun invoke(savedMix: SavedMix) {
        val allSounds = soundRepository.getSounds().first()

        val mixMap = savedMix.sounds.mapNotNull { savedSound ->
            val realSound = allSounds.find { it.id == savedSound.id }
            if (realSound != null) {
                realSound to savedSound.volume
            } else {
                null
            }
        }.toMap()

        if (mixMap.isNotEmpty()) {
            // todo: aç bunu
//            soundManager.setMix(mixMap)
        }
    }
}