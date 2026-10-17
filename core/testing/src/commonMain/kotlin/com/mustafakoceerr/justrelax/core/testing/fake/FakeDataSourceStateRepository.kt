package com.mustafakoceerr.justrelax.core.testing.fake

import com.mustafakoceerr.justrelax.core.domain.repository.sound.DataSourceStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeDataSourceStateRepository(lastSyncTimestamp: Long = 0L) : DataSourceStateRepository {

    val lastSync = MutableStateFlow(lastSyncTimestamp)

    override fun getLastSoundSyncTimestamp(): Flow<Long> = lastSync

    override suspend fun setLastSoundSyncTimestamp(timestamp: Long) {
        lastSync.value = timestamp
    }
}
