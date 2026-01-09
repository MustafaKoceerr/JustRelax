package com.mustafakoceerr.justrelax.feature.mixer.usecase

import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.coroutines.flow.first
import kotlin.random.Random

class GenerateRandomMixUseCase(
    private val soundRepository: SoundRepository
) {
    suspend operator fun invoke(count: Int): Map<Sound, Float> {
        val downloadedSounds = soundRepository.getSounds().first().filter { it.isDownloaded }

        return downloadedSounds
            .shuffled()
            .take(count)
            .associateWith {
                (Random.nextFloat() * 0.55f) + 0.3f
            }
    }
}