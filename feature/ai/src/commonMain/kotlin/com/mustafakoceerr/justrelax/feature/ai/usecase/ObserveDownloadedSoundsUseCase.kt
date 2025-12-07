package com.mustafakoceerr.justrelax.feature.ai.usecase

import com.mustafakoceerr.justrelax.core.sound.domain.repository.SoundRepository
import kotlinx.coroutines.flow.Flow

class ObserveDownloadedSoundsUseCase(
    private val repository: SoundRepository
) {
    operator fun invoke(): Flow<List<Sound>> {
        return repository.getDownloadedSounds()
    }
}