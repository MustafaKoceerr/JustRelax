package com.mustafakoceerr.justrelax.core.data.factory

import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

internal fun createDataStore(
    producePath: () -> String,
    migrations: List<DataMigration<Preferences>> = emptyList(),
): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        migrations = migrations,
        produceFile = { producePath().toPath() }
    )
