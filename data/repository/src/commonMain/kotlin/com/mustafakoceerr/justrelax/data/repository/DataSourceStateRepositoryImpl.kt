package com.mustafakoceerr.justrelax.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.domain.repository.sound.DataSourceStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class DataSourceStateRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val dispatchers: DispatcherProvider
) : DataSourceStateRepository {

    override fun getLastSoundSyncTimestamp(): Flow<Long> {
        return dataStore.data.map { preferences ->
            // Anahtarı merkezi 'DataConstants' dosyasından alıyoruz.
            preferences[DataConstants.KEY_LAST_SOUND_SYNC] ?: 0L
        }
    }

    override suspend fun setLastSoundSyncTimestamp(timestamp: Long) {
        withContext(dispatchers.io) {
            dataStore.edit { preferences ->
                // Anahtarı merkezi 'DataConstants' dosyasından alıyoruz.
                preferences[DataConstants.KEY_LAST_SOUND_SYNC] = timestamp
            }
        }
    }
}