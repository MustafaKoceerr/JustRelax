package com.mustafakoceerr.justrelax.core.domain.usecase.sound

import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.coroutines.flow.Flow

class GetSoundsUseCase(
    private val soundRepository: SoundRepository
) {
    operator fun invoke(): Flow<List<Sound>> = soundRepository.getSounds()
}