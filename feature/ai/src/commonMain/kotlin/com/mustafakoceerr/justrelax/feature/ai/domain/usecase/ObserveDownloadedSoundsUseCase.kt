package com.mustafakoceerr.justrelax.feature.ai.domain.usecase

import kotlinx.coroutines.flow.Flow

class ObserveDownloadedSoundsUseCase(
    private val repository: SoundRepository
) {
    operator fun invoke(): Flow<List<Sound>> {
        return repository.getDownloadedSounds()
    }
}