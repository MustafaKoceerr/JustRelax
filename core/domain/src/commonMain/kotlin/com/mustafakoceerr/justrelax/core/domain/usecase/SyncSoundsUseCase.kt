package com.mustafakoceerr.justrelax.core.domain.usecase

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.repository.DataSourceStateRepository
import com.mustafakoceerr.justrelax.core.domain.repository.SoundSyncRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Sorumluluk: Sunucudaki ses listesini yerel veritabanıyla senkronize etmek.
 * Zaman kontrolü yapmaz, direkt tetiklenir.
 */
class SyncSoundsUseCase(
    private val soundSyncRepository: SoundSyncRepository,
    private val dataSourceStateRepository: DataSourceStateRepository
) {
    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(): Resource<Unit> {
        val result = soundSyncRepository.syncWithServer()
// Başarılı olursa zaman damgasını güncelle
        if (result is Resource.Success) {
            dataSourceStateRepository.setLastSoundSyncTimestamp(
                Clock.System.now().toEpochMilliseconds()
            )
        }
        return result
    }
}
