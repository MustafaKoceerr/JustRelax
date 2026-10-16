package com.mustafakoceerr.justrelax.feature.mixer.usecase

import com.mustafakoceerr.justrelax.core.data.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.data.language.LanguageController
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.core.model.localized
import kotlinx.coroutines.flow.first
import kotlin.random.Random

class GenerateRandomMixUseCase(
    private val soundRepository: SoundRepository,
    private val languageController: LanguageController,
    private val random: Random = Random,
) {
    suspend operator fun invoke(count: Int): Map<LocalizedSound, Float> {
        val allSounds = soundRepository.getSounds().first()

        val downloadedSounds = allSounds.filter { it.isDownloaded }

        if (downloadedSounds.isEmpty()) return emptyMap()

        val selectedSounds = downloadedSounds.shuffled(random).take(count)

        val currentLanguage = languageController.getCurrentLanguage()

        return selectedSounds.associate { sound ->
            val randomVolume = 0.2f + random.nextFloat() * 0.7f // 0.2 - 0.9
            sound.localized(currentLanguage.code) to randomVolume
        }
    }
}