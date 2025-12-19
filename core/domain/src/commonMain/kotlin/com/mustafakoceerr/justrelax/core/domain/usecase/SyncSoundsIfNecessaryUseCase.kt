package com.mustafakoceerr.justrelax.core.domain.usecase

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.repository.DataSourceStateRepository
import com.mustafakoceerr.justrelax.core.domain.repository.SoundSyncRepository
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

/*
Sorumluluk (SRP): Bu sınıfın değişmesi için tek bir sebep vardır:
 "Senkronizasyonun ne zaman gerekli olduğuna dair iş kuralının değişmesi."
  (Örneğin, 24 saat kuralını 12 saate indirmek veya "sadece Wi-Fi'deyken sync et" gibi yeni bir kural eklemek).
   Senkronizasyonun nasıl yapıldığı (SoundSyncRepository) veya son sync zamanının nerede saklandığı (DataSourceStateRepository) değişirse,
bu sınıf etkilenmez.
 */
/**
 * Sorumluluk: Ses verilerinin en son senkronizasyon zamanını kontrol ederek,
 * belirlenen süre (TTL - Time-to-Live) geçmişse senkronizasyon işlemini tetiklemek.
 */

class SyncSoundsIfNecessaryUseCase (
    private val dataSourceStateRepository: DataSourceStateRepository,
    private val soundSyncRepository: SoundSyncRepository
) {
    // 'invoke' fonksiyonunu 'operator' olarak işaretleyerek,
    // bu sınıfı doğrudan 'useCase()' şeklinde bir fonksiyon gibi çağırabilmemizi sağlarız.

    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(): Resource<Unit> {
        // 1. Son başarılı sync zamanını al. 'first()' ile Flow'dan tek bir değer okuyoruz.
        val lastSyncTimestamp = dataSourceStateRepository.getLastSoundSyncTimestamp().first()
        val now = Clock.System.now().toEpochMilliseconds()

        // 2. İş Kuralı: Son sync'in üzerinden 24 saatten fazla geçti mi?
        val twentyFourHoursInMillis = 24.hours.inWholeMilliseconds
        if ((now - lastSyncTimestamp) > twentyFourHoursInMillis) {
            // 3. Kural geçerliyse, sync işlemini tetikle.
            val syncResult = soundSyncRepository.syncWithServer()

            // 4. Sync işlemi başarılıysa, yeni timestamp'i kaydet.
            if (syncResult is Resource.Success) {
                dataSourceStateRepository.setLastSoundSyncTimestamp(now)
            }
            return syncResult
        }

        // 5. Süre dolmamışsa, hiçbir şey yapma ve başarılı olarak kabul et.
        return Resource.Success(Unit)
    }
}