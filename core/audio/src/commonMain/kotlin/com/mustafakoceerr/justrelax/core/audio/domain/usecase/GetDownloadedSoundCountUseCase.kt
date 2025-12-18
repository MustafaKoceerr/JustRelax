package com.mustafakoceerr.justrelax.core.audio.domain.usecase

import com.mustafakoceerr.justrelax.core.domain.repository.SoundRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetDownloadedSoundCountUseCase(
    private val repository: SoundRepository
) {
    operator fun invoke(): Flow<Int> {
        return repository.getDownloadedSounds().map { it.size }
    }
}