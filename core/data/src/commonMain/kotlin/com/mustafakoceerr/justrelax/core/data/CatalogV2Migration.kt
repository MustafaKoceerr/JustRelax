package com.mustafakoceerr.justrelax.core.data

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences

/**
 * The v2 catalog starts from an empty database, so a v1 install must download its sounds and sync
 * again: it forgets that the starter pack was installed and when it last synced. Theme and
 * language are kept.
 */
internal object CatalogV2Migration : DataMigration<Preferences> {
    private const val CATALOG_VERSION = 2

    override suspend fun shouldMigrate(currentData: Preferences): Boolean =
        (currentData[DataConstants.KEY_CATALOG_VERSION] ?: 1) < CATALOG_VERSION

    override suspend fun migrate(currentData: Preferences): Preferences =
        currentData.toMutablePreferences().apply {
            remove(DataConstants.KEY_STARTER_PACK_INSTALLED)
            remove(DataConstants.KEY_LAST_SOUND_SYNC)
            this[DataConstants.KEY_CATALOG_VERSION] = CATALOG_VERSION
        }.toPreferences()

    override suspend fun cleanUp() = Unit
}
