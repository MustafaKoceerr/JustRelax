package com.mustafakoceerr.justrelax.feature.saved.usecase

import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMix
import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMixRepository

class RestoreSavedMixUseCase(
    private val savedMixRepository: SavedMixRepository
) {
    suspend operator fun invoke(savedMix: SavedMix) {
        val soundVolumes = savedMix.sounds.entries.associate { (sound, volume) ->
            sound.id to volume
        }
        savedMixRepository.saveMix(savedMix.name, soundVolumes)
    }
}