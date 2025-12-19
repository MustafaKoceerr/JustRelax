package com.mustafakoceerr.justrelax.feature.mixer.usecase

import kotlinx.coroutines.flow.first
import kotlin.random.Random

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
        // 3. Rastgele Ses Seviyesi Ata (%20 ile %90 arası)
        return selectedSounds.associateWith {
            // (0..1 * 0.7) + 0.2  => Sonuç 0.2 ile 0.9 arası olur.
            val randomVolume = (Random.nextFloat() * 0.7f) + 0.2f

            randomVolume
    }
}
}