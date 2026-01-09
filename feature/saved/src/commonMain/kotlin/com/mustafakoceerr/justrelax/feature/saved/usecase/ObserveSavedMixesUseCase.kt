package com.mustafakoceerr.justrelax.feature.saved.usecase

import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMix
import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMixRepository
import kotlinx.coroutines.flow.Flow

class ObserveSavedMixesUseCase(
    private val savedMixRepository: SavedMixRepository
) {
    operator fun invoke(): Flow<List<SavedMix>> {
        return savedMixRepository.getSavedMixes()
    }
}