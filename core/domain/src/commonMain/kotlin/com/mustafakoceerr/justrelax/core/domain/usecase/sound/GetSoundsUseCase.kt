package com.mustafakoceerr.justrelax.core.domain.usecase.sound


import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.coroutines.flow.Flow

class GetSoundsUseCase(
    private val soundRepository: SoundRepository
) {
    /**
     * Tüm seslerin listesini akış olarak döner.
     * Home Screen listeleme ve Onboarding MB hesabı için kullanılır.
     */
    operator fun invoke(): Flow<List<Sound>> {
        return soundRepository.getSounds()
    }
}