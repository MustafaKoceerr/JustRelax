package com.mustafakoceerr.justrelax.feature.saved.usecase

import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMixRepository

class DeleteSavedMixUseCase(
    private val savedMixRepository: SavedMixRepository
) {
    suspend operator fun invoke(mixId: Long) {
        savedMixRepository.deleteMix(mixId)
    }
}