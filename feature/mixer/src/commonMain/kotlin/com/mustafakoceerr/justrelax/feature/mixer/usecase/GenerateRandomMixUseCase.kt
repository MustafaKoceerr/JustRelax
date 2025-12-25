package com.mustafakoceerr.justrelax.feature.mixer.usecase

import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.coroutines.flow.first
import kotlin.random.Random

/**
 * Sorumluluk (SRP):
 * Sadece indirilmiş seslerden, belirtilen sayıda rastgele bir mix oluşturmak.
 * Ses seviyelerini de rastgele atar.
 *
 * Varsayım: Bu UseCase çağrıldığında, sistemde her zaman yeterli sayıda indirilmiş
 * ses olduğu varsayılır (Onboarding'de bu garanti altına alınmıştır).
 */
class GenerateRandomMixUseCase(
    private val soundRepository: SoundRepository
) {
    suspend operator fun invoke(count: Int): Map<Sound, Float> {
        // 1. İndirilmiş sesleri al
        val downloadedSounds = soundRepository.getSounds().first().filter { it.isDownloaded }

        // 2. Sesleri karıştır, istenen sayıda al ve rastgele ses seviyesi ata
        // Not: take() fonksiyonu güvenlidir. Listede 5 eleman varken take(8) dersen 5 tane verir, çökmez.
        // Ama biz zaten yeterli ses olduğundan eminiz.
        return downloadedSounds
            .shuffled()
            .take(count)
            .associateWith {
                // Ses seviyesini %30 ile %85 arasında rastgele ata
                (Random.nextFloat() * 0.55f) + 0.3f
            }
    }
}