package com.mustafakoceerr.justrelax.core.domain.usecase.savedmix

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMixRepository
import com.mustafakoceerr.justrelax.core.domain.usecase.player.GetGlobalMixerStateUseCase

class SaveCurrentMixUseCase(
    private val savedMixRepository: SavedMixRepository,
    private val getGlobalMixerStateUseCase: GetGlobalMixerStateUseCase
) {
    suspend operator fun invoke(name: String): Resource<Unit> {
        if (name.isBlank()) {
            return Resource.Error(AppError.SaveMix.EmptyName())
        }

        val activeSounds = getGlobalMixerStateUseCase().value.activeSounds
        if (activeSounds.isEmpty()) {
            return Resource.Error(AppError.SaveMix.NoSoundsPlaying())
        }

        val soundsToSave = activeSounds.associate { config ->
            config.id to config.initialVolume
        }

        return try {
            savedMixRepository.saveMix(name, soundsToSave)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(AppError.Database.SaveFailed(e.message))
        }
    }
}