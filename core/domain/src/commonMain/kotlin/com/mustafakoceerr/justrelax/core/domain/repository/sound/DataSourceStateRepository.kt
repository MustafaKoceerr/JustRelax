package com.mustafakoceerr.justrelax.core.domain.repository.sound

import kotlinx.coroutines.flow.Flow

interface DataSourceStateRepository {
    fun getLastSoundSyncTimestamp(): Flow<Long>
    suspend fun setLastSoundSyncTimestamp(timestamp: Long)
}