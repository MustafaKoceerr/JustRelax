package com.mustafakoceerr.justrelax.core.data

import androidx.datastore.preferences.core.preferencesOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SettingsOnlyMigrationTest {

    private val oldSettings = preferencesOf(
        DataConstants.KEY_APP_THEME to "DARK",
        DataConstants.KEY_APP_LANGUAGE to "tr",
        DataConstants.KEY_STARTER_PACK_INSTALLED to true,
        DataConstants.KEY_LAST_SOUND_SYNC to 1_000L,
        DataConstants.KEY_CATALOG_VERSION to 2,
    )

    @Test
    fun theAppStateLeavesTheBackedUpSettingsFile_andTheSettingsStay() = runTest {
        assertTrue(SettingsOnlyMigration.shouldMigrate(oldSettings))

        val migrated = SettingsOnlyMigration.migrate(oldSettings)

        assertNull(migrated[DataConstants.KEY_STARTER_PACK_INSTALLED])
        assertNull(migrated[DataConstants.KEY_LAST_SOUND_SYNC])
        assertNull(migrated[DataConstants.KEY_CATALOG_VERSION])
        assertEquals("DARK", migrated[DataConstants.KEY_APP_THEME])
        assertEquals("tr", migrated[DataConstants.KEY_APP_LANGUAGE])
    }

    @Test
    fun itRunsOnce() = runTest {
        assertFalse(SettingsOnlyMigration.shouldMigrate(SettingsOnlyMigration.migrate(oldSettings)))
    }
}
