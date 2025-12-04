package com.mustafakoceerr.justrelax.feature.mixer.domain.usecase

import com.mustafakoceerr.justrelax.core.sound.domain.model.Sound
import com.mustafakoceerr.justrelax.core.sound.domain.repository.SoundRepository
import kotlinx.coroutines.flow.first
import kotlin.random.Random

class GenerateRandomMixUseCase(
    private val soundRepository: SoundRepository
) {
    /**
     * Rastgele sesler seçer ve her birine 0.2 ile 0.9 arasında rastgele ses seviyesi atar.
     * Geriye Ses ve Volume bilgisini içeren bir Map döndürür.
     */
    suspend operator fun invoke(count: Int): Map<Sound, Float> {
        val allSounds = soundRepository.getSounds().first()

        // 1. Kural: Bir ses birden fazla seçilemez (shuffled ve take bunu garanti eder)
        val selectedSounds = allSounds.shuffled().take(count)

        // 2. Kural: Ses yüksekliği %20-90 arasında olmalı
        return selectedSounds.associateWith {
            // 0.2 ile 0.9 arasında random float üretir
            Random.nextDouble(0.2, 0.9).toFloat()
        }
    }
}