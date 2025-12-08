package com.mustafakoceerr.justrelax.feature.saved.usecase

import com.mustafakoceerr.justrelax.core.common.formatEpoch
import com.mustafakoceerr.justrelax.core.domain.repository.SavedMixRepository
import com.mustafakoceerr.justrelax.core.domain.repository.SoundRepository
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedMixUiModel
import com.mustafakoceerr.justrelax.core.common.formatTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
// import com.mustafakoceerr.justrelax.core.common.util.TimeUtils.formatDate (Varsa)

class ObserveSavedMixesUseCase(
    private val savedMixRepository: SavedMixRepository,
    private val soundRepository: SoundRepository
) {
    operator fun invoke(): Flow<List<SavedMixUiModel>> {
        return combine(
            savedMixRepository.getAllMixes(),
            soundRepository.getSounds()
        ) { savedMixes, allSounds ->
            savedMixes.map { domainMix ->
                val icons = domainMix.sounds.mapNotNull { savedSound ->
                    allSounds.find { it.id == savedSound.id }?.iconUrl
                }

                // 2. Logic: UI Modelini oluştur
                SavedMixUiModel(
                    id = domainMix.id,
                    title = domainMix.name,
                    date = formatEpoch(domainMix.dateEpoch), // Tarih formatlama burada
                    icons = icons,
                    domainModel = domainMix
                )
            }
        }
    }
}