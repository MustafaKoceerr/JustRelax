package com.mustafakoceerr.justrelax.feature.player.usecase

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.data.repository.SavedMixRepository
import com.mustafakoceerr.justrelax.core.data.repository.SceneRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

class SaveCurrentMixUseCase(
    private val savedMixRepository: SavedMixRepository,
    private val sceneRepository: SceneRepository
) {
    suspend operator fun invoke(name: String): Resource<Unit> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Resource.Error(AppError.SaveMix.EmptyName())
        }

        val layers = sceneRepository.scene.value.layers
        if (layers.isEmpty()) {
            return Resource.Error(AppError.SaveMix.NoSoundsPlaying())
        }

        val nameTaken = savedMixRepository.getSavedMixes().first()
            .any { it.name.equals(trimmedName, ignoreCase = true) }
        if (nameTaken) {
            return Resource.Error(AppError.SaveMix.NameAlreadyExists())
        }

        val soundsToSave = layers.associate { layer ->
            layer.soundId to layer.volume
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