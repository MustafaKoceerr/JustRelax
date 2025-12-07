package com.mustafakoceerr.justrelax.feature.saved.usecase

import com.mustafakoceerr.justrelax.core.database.domain.repository.SavedMixRepository
import com.mustafakoceerr.justrelax.core.sound.domain.repository.SoundRepository
import com.mustafakoceerr.justrelax.feature.saved.SavedMixUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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
                // 1. Logic: İkonları eşleştir
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

    // Tarih formatlama logic'i (Helper)
    @OptIn(ExperimentalTime::class)
    private fun formatEpoch(epochMillis: Long): String {
        val instant = Instant.fromEpochMilliseconds(epochMillis)
        val date = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        // Örn: 12.10.2025
        return "${date.day}.${date.month.number}.${date.year}"
    }
}