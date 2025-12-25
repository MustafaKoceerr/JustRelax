package com.mustafakoceerr.justrelax.core.domain.usecase.savedmix

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.common.asResource
import com.mustafakoceerr.justrelax.core.domain.controller.SoundController
import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMixRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * Sorumluluk (SRP):
 * O an çalmakta olan sesleri ve onların ses seviyelerini, verilen isimle
 * doğrulamadan geçirerek kaydeder.
 */
class SaveCurrentMixUseCase(
    private val savedMixRepository: SavedMixRepository
) {
    // SoundController'ı buraya parametre olarak ekledik 👇
    operator fun invoke(name: String, soundController: SoundController): Flow<Resource<Unit>> = flow {
        if (name.isBlank()) throw AppError.SaveMix.EmptyName

        // Parametre olarak gelen controller'dan durumu al
        val currentState = soundController.state.first()
        val playingIds = currentState.playingSoundIds
        val volumes = currentState.soundVolumes

        if (playingIds.isEmpty()) throw AppError.SaveMix.NoSoundsPlaying

        val soundsToSave = playingIds.associateWith { id ->
            volumes[id] ?: 0.5f
        }

        savedMixRepository.saveMix(name, soundsToSave)

        emit(Unit)
    }.asResource()
}