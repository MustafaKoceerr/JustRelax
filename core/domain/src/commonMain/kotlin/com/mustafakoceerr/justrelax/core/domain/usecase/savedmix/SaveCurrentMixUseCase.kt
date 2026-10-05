package com.mustafakoceerr.justrelax.core.domain.usecase.savedmix

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMixRepository
import com.mustafakoceerr.justrelax.core.domain.usecase.player.GetGlobalMixerStateUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

class SaveCurrentMixUseCase(
    private val savedMixRepository: SavedMixRepository,
    private val getGlobalMixerStateUseCase: GetGlobalMixerStateUseCase
) {
    suspend operator fun invoke(name: String): Resource<Unit> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Resource.Error(AppError.SaveMix.EmptyName())
        }

        val activeSounds = getGlobalMixerStateUseCase().value.activeSounds
        if (activeSounds.isEmpty()) {
            return Resource.Error(AppError.SaveMix.NoSoundsPlaying())
        }

        val nameTaken = savedMixRepository.getSavedMixes().first()
            .any { it.name.equals(trimmedName, ignoreCase = true) }
        if (nameTaken) {
            return Resource.Error(AppError.SaveMix.NameAlreadyExists())
        }

        val soundsToSave = activeSounds.associate { config ->
            config.id to config.initialVolume
        }

        return try {
            savedMixRepository.saveMix(trimmedName, soundsToSave)
            Resource.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Resource.Error(AppError.Database.SaveFailed(e.message))
        }
    }
}