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

        // ID eşleşmesi yaparak çalınacak listeyi hazırla
        val mixMap = mutableMapOf<Sound, Float>()

        savedMix.sounds.forEach { savedSound ->
            val realSound = allSounds.find { it.id == savedSound.id }
            // Sadece indirilmiş sesleri çal (Güvenlik)
            if (realSound != null && realSound.isDownloaded) {
                mixMap[realSound] = savedSound.volume
            }
        }

        if (mixMap.isNotEmpty()) {
            // Fire and Forget: Sadece çal, ID göndermene gerek yok (-1 gider)
            soundManager.setMix(mixMap)
        }
    }
}