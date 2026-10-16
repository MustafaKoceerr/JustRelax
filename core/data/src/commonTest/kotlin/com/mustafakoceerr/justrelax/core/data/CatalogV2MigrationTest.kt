package com.mustafakoceerr.justrelax.core.data

import androidx.datastore.preferences.core.preferencesOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CatalogV2MigrationTest {

    private val v1Settings = preferencesOf(
        DataConstants.KEY_APP_THEME to "DARK",
        DataConstants.KEY_STARTER_PACK_INSTALLED to true,
        DataConstants.KEY_LAST_SOUND_SYNC to 1_000L,
    )

    @Test
    fun aV1Install_forgetsItsDownloadsAndLastSync_butKeepsItsSettings() = runTest {
        assertTrue(CatalogV2Migration.shouldMigrate(v1Settings))

        val migrated = CatalogV2Migration.migrate(v1Settings)

        assertNull(migrated[DataConstants.KEY_STARTER_PACK_INSTALLED])
        assertNull(migrated[DataConstants.KEY_LAST_SOUND_SYNC])
        assertEquals("DARK", migrated[DataConstants.KEY_APP_THEME])
    }

    @Test
    fun itRunsOnce() = runTest {
        assertFalse(CatalogV2Migration.shouldMigrate(CatalogV2Migration.migrate(v1Settings)))
    }
}
