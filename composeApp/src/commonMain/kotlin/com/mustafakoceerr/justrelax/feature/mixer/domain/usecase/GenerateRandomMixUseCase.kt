package com.mustafakoceerr.justrelax.feature.mixer.domain.usecase

import com.mustafakoceerr.justrelax.core.sound.domain.repository.SoundRepository
import kotlinx.coroutines.flow.first

class GenerateRandomMixUseCase(
    private val soundRepository: SoundRepository
) {
    suspend operator fun invoke(requestedCount: Int): Map<Sound, Float> {
        // 1. Sadece indirilmiş sesleri çek (Zaten en az 8 tane var)
        val downloadedSounds = soundRepository.getDownloadedSounds().first()

        // 2. Karıştır ve istenen sayı kadar al
        // Not: take() fonksiyonu güvenlidir. Listede 5 eleman varken take(8) dersen 5 tane verir, patlamaz.
        val selectedSounds = downloadedSounds.shuffled().take(requestedCount)

        // 3. Hepsine varsayılan ses seviyesi ata (0.5f) ve döndür
        return selectedSounds.associateWith { 0.5f }
    }
}