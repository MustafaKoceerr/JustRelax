package com.mustafakoceerr.justrelax.feature.mixer.usecase

import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.domain.system.LanguageController
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.core.model.localized
import kotlinx.coroutines.flow.first
import kotlin.random.Random

class GenerateRandomMixUseCase(
    private val soundRepository: SoundRepository,
    private val languageController: LanguageController
) {
    suspend operator fun invoke(count: Int): Map<LocalizedSound, Float> {
        val allSounds = soundRepository.getSounds().first()

        val downloadedSounds = allSounds.filter { it.isDownloaded }

        if (downloadedSounds.isEmpty()) return emptyMap()

        val selectedSounds = downloadedSounds.shuffled().take(count)

        val currentLanguage = languageController.getCurrentLanguage()

        return selectedSounds.associate { sound ->
            val randomVolume = 0.2f + Random.nextFloat() * 0.7f // 0.2 - 0.9
            sound.localized(currentLanguage.code) to randomVolume
        }
    }
}