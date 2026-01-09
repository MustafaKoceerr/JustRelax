package com.mustafakoceerr.justrelax.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.domain.repository.appsetup.AppSetupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class AppSetupRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val dispatchers: DispatcherProvider
) : AppSetupRepository {

    override val isStarterPackInstalled: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[DataConstants.KEY_STARTER_PACK_INSTALLED] ?: false
        }

    override suspend fun setStarterPackInstalled(installed: Boolean) {
        withContext(dispatchers.io) {
            dataStore.edit { preferences ->
                preferences[DataConstants.KEY_STARTER_PACK_INSTALLED] = installed
            }
        }
    }
}