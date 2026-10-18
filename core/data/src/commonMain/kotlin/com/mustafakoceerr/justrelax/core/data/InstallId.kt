package com.mustafakoceerr.justrelax.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mustafakoceerr.justrelax.core.network.api.InstallIdProvider
import kotlinx.coroutines.flow.first

/**
 * A random id made on first use and kept in the state file, which is not backed up, so a new
 * phone gets a new id. It names this install to the API proxy's rate limits; it is not linked to
 * the user.
 */
internal class DataStoreInstallIdProvider(
    private val dataStore: DataStore<Preferences>,
    private val newId: () -> String,
) : InstallIdProvider {

    override suspend fun installId(): String {
        dataStore.data.first()[KEY]?.let { return it }
        var id = ""
        dataStore.edit { preferences -> id = preferences[KEY] ?: newId().also { preferences[KEY] = it } }
        return id
    }

    private companion object {
        val KEY = stringPreferencesKey("install_id")
    }
}
