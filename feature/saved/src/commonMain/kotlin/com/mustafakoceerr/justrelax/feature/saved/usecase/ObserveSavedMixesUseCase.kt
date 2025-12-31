package com.mustafakoceerr.justrelax.feature.saved.usecase

import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMix
import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMixRepository
import kotlinx.coroutines.flow.Flow

/**
 * Sorumluluk (SRP):
 * Veritabanındaki kaydedilmiş mix'leri dinlemek ve bunları UI'ın anlayacağı
 * bir modele (SavedMixUiModel) dönüştürmek.
 */
class ObserveSavedMixesUseCase(
    private val savedMixRepository: SavedMixRepository
) {
    // UI Model yerine Domain Model döndür
    operator fun invoke(): Flow<List<SavedMix>> {
        return savedMixRepository.getSavedMixes()
    }
}