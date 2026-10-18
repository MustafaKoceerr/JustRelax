package com.mustafakoceerr.justrelax.core.data

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences

/**
 * The settings file is the only one Android backs up, so it holds the user's choices only. The
 * app's own state (starter pack, last sync, catalog version) lives in the state file; restoring it
 * on a new phone would skip the downloads that phone still needs. Older installs kept that state
 * here, so it is removed. A v1 install then starts with an empty state file and downloads the v2
 * catalog, as it must.
 */
internal object SettingsOnlyMigration : DataMigration<Preferences> {
    private val appStateKeys = listOf(
        DataConstants.KEY_STARTER_PACK_INSTALLED,
        DataConstants.KEY_LAST_SOUND_SYNC,
        DataConstants.KEY_CATALOG_VERSION,
    )

    override suspend fun shouldMigrate(currentData: Preferences): Boolean =
        appStateKeys.any { it in currentData }

    override suspend fun migrate(currentData: Preferences): Preferences =
        currentData.toMutablePreferences().apply { appStateKeys.forEach { remove(it) } }.toPreferences()

    override suspend fun cleanUp() = Unit
}
