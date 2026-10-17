package com.mustafakoceerr.justrelax.core.data.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeSyncTimestampStore(lastSyncTimestamp: Long = 0L) : SyncTimestampStore {

    val lastSync = MutableStateFlow(lastSyncTimestamp)

    override fun getLastSoundSyncTimestamp(): Flow<Long> = lastSync

    override suspend fun setLastSoundSyncTimestamp(timestamp: Long) {
        lastSync.value = timestamp
    }
}
