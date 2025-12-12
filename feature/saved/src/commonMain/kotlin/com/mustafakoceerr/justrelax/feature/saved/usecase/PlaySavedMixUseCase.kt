package com.mustafakoceerr.justrelax.feature.saved.usecase

import com.mustafakoceerr.justrelax.core.audio.SoundManager
import com.mustafakoceerr.justrelax.core.domain.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.model.SavedMix
import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.coroutines.flow.first


class PlaySavedMixUseCase(
    private val soundRepository: SoundRepository,
    private val soundManager: SoundManager
) {
    suspend operator fun invoke(savedMix: SavedMix) {
        val allSounds = soundRepository.getSounds().first()

// 2. SavedMix içindeki ID'leri gerçek Sound objelerine eşle
        val mixMap = mutableMapOf<Sound, Float>()

        savedMix.sounds.forEach { savedSound ->
            val realSound = allSounds.find { it.id == savedSound.id }
            // Eğer ses bulunursa ve indirilmişse listeye ekle
            if (realSound != null && realSound.isDownloaded) {
                mixMap[realSound] = savedSound.volume
            }
        }

        // 3. Eğer çalınacak ses varsa SoundManager'a gönder
        if (mixMap.isNotEmpty()) {
            soundManager.setMix(mixMap)
        }
    }
}