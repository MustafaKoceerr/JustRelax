package com.mustafakoceerr.justrelax.core.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Sorumluluk: Uygulamanın çeşitli veri kaynaklarının (örn: Sesler, Haberler)
 * durumu ve senkronizasyon zamanlaması gibi metadata'larını yönetmek.
 */
interface DataSourceStateRepository {

    /**
     * Ses verisinin en son ne zaman başarılı bir şekilde senkronize edildiğini
     * 'timestamp' (Long) olarak döndürür.
     */
    fun getLastSoundSyncTimestamp(): Flow<Long>

    /**
     * Ses verisi senkronizasyonu başarılı olduğunda, şu anki zamanı kaydeder.
     */
    suspend fun setLastSoundSyncTimestamp(timestamp: Long)
}