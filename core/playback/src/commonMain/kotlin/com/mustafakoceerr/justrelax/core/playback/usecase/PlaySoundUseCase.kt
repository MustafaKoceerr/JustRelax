package com.mustafakoceerr.justrelax.core.playback.usecase

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.playback.soundscape.SoundscapeEngine
import com.mustafakoceerr.justrelax.core.data.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import kotlinx.coroutines.flow.first

/** Adds a downloaded sound to the current scene. */
class PlaySoundUseCase(
    private val soundRepository: SoundRepository,
    private val soundscapeEngine: SoundscapeEngine,
) {
    suspend operator fun invoke(
        soundId: String,
        volume: Float = AudioDefaults.BASE_VOLUME,
    ): Resource<Unit> {
        val sound = soundRepository.getSound(soundId).first()
            ?: return Resource.Error(AppError.Storage.FileNotFound())

        val localPath = sound.localPath
        if (localPath.isNullOrBlank()) {
            return Resource.Error(AppError.Storage.FileNotFound())
        }

        // Every catalog sound is a bed until the v2 catalog brings event sounds.
        return soundscapeEngine.addLayer(Layer.Bed(sound.id, clip = localPath, volume = volume.coerceIn(0f, 1f)))
    }
}
