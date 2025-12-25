package com.mustafakoceerr.justrelax.feature.saved.usecase

import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMix
import com.mustafakoceerr.justrelax.core.domain.repository.savedmix.SavedMixRepository

/**
 * Sorumluluk (SRP):
 * Silinmiş bir mix'i (SavedMix nesnesini) tekrar veritabanına kaydetmek (Undo işlemi).
 */
class RestoreSavedMixUseCase(
    private val savedMixRepository: SavedMixRepository
) {
    suspend operator fun invoke(savedMix: SavedMix) {
        // Domain Model (Map<Sound, Float>) -> Repository Formatı (Map<SoundID, Volume>)
        // Sound nesnesinden sadece ID'yi alıp volümle eşleştiriyoruz.
        val soundVolumes = savedMix.sounds.entries.associate { (sound, volume) ->
            sound.id to volume
        }

        savedMixRepository.saveMix(savedMix.name, soundVolumes)
    }
}