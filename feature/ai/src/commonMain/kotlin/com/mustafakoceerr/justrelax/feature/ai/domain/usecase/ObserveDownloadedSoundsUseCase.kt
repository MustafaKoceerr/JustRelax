package com.mustafakoceerr.justrelax.feature.ai.domain.usecase

import com.mustafakoceerr.justrelax.core.domain.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.coroutines.flow.Flow

class ObserveDownloadedSoundsUseCase(
    private val repository: SoundRepository
) {
    operator fun invoke(): Flow<List<Sound>> {
        return repository.getDownloadedSounds()
    }
}