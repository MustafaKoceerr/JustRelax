package com.mustafakoceerr.justrelax.core.data.factory

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.mustafakoceerr.justrelax.core.data.CatalogV2Migration
import okio.Path.Companion.toPath

internal fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        migrations = listOf(CatalogV2Migration),
        produceFile = { producePath().toPath() }
    )
