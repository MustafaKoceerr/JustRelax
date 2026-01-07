package com.mustafakoceerr.justrelax.core.domain.usecase.player

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import com.mustafakoceerr.justrelax.core.domain.player.SoundConfig
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import kotlinx.coroutines.flow.first

class PlaySoundUseCase(
    private val soundRepository: SoundRepository,
    private val audioMixer: AudioMixer
) {
    suspend operator fun invoke(
        soundId: String,
        initialVolume: Float = AudioDefaults.BASE_VOLUME
    ): Resource<Unit> {
        val sound = soundRepository.getSound(soundId).first()
            ?: return Resource.Error(AppError.Storage.FileNotFound())

        val localPath = sound.localPath
        if (localPath.isNullOrBlank()) {
            return Resource.Error(AppError.Storage.FileNotFound())
        }

        val config = SoundConfig(
            id = sound.id,
            url = localPath,
            initialVolume = initialVolume.coerceIn(0f, 1f),
            fadeInDurationMs = AudioDefaults.FADE_IN_MS
        )

        return audioMixer.playSound(config)
    }
}