package com.mustafakoceerr.justrelax.feature.saved.usecase

import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMixRepository
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedMixUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Sorumluluk (SRP):
 * Veritabanındaki kaydedilmiş mix'leri dinlemek ve bunları UI'ın anlayacağı
 * bir modele (SavedMixUiModel) dönüştürmek.
 */
class ObserveSavedMixesUseCase(
    private val savedMixRepository: SavedMixRepository
) {
    operator fun invoke(): Flow<List<SavedMixUiModel>> {
        return savedMixRepository.getSavedMixes()
            .map { domainMixes ->
                // Domain modelini (SavedMix) -> UI modeline (SavedMixUiModel) çevir
                domainMixes.map { domainMix ->
                    SavedMixUiModel(
                        id = domainMix.id,
                        title = domainMix.name,
                        date = domainMix.createdAt, // Tarih formatlama burada yapılabilir
                        icons = domainMix.sounds.keys.map { it.iconUrl },
                        domainModel = domainMix // Orijinal modeli de saklıyoruz (Play/Delete için)
                    )
                }
            }
    }
}