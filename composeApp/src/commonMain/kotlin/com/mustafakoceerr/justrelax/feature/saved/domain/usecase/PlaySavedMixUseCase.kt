package com.mustafakoceerr.justrelax.feature.saved.domain.usecase

import com.mustafakoceerr.justrelax.core.database.domain.model.SavedMix
import com.mustafakoceerr.justrelax.core.sound.domain.manager.SoundManager
import com.mustafakoceerr.justrelax.core.sound.domain.repository.SoundRepository
import kotlinx.coroutines.flow.first


class PlaySavedMixUseCase(
    private val soundRepository: SoundRepository,
    private val soundManager: SoundManager
) {
    suspend operator fun invoke(savedMix: SavedMix) {
        // 1. Repository'den güncel ses listesini tek seferlik al (Flow'u beklemiyoruz, anlık veri lazım)
        val allSounds = soundRepository.getSounds().first()

        // 2. SavedMix içindeki ID'leri gerçek Sound nesneleriyle eşleştir
        val mixMap = savedMix.sounds.mapNotNull { savedSound ->
            val realSound = allSounds.find { it.id == savedSound.id }
            if (realSound != null) {
                realSound to savedSound.volume
            } else {
                null // Eğer ses sistemden silindiyse listeye ekleme
            }
        }.toMap()

        // 3. Eğer liste boş değilse SoundManager'a ilet
        if (mixMap.isNotEmpty()) {
            soundManager.setMix(mixMap)
        }
    }
}