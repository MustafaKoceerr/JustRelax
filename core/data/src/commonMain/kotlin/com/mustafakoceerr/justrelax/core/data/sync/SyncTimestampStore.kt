package com.mustafakoceerr.justrelax.core.data.sync

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.data.DataConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** When the sound catalog was last synced successfully, in epoch milliseconds (0 = never). */
internal interface SyncTimestampStore {
    fun getLastSoundSyncTimestamp(): Flow<Long>
    suspend fun setLastSoundSyncTimestamp(timestamp: Long)
}

internal class DataStoreSyncTimestampStore(
    private val dataStore: DataStore<Preferences>,
    private val dispatchers: DispatcherProvider
) : SyncTimestampStore {

    override fun getLastSoundSyncTimestamp(): Flow<Long> {
        return dataStore.data.map { preferences ->
            preferences[DataConstants.KEY_LAST_SOUND_SYNC] ?: 0L
        }
    }

    override suspend fun setLastSoundSyncTimestamp(timestamp: Long) {
        withContext(dispatchers.io) {
            dataStore.edit { preferences ->
                preferences[DataConstants.KEY_LAST_SOUND_SYNC] = timestamp
            }
        }
    }
}