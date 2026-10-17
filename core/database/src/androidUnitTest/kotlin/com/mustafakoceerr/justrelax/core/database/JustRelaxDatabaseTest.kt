package com.mustafakoceerr.justrelax.core.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.mustafakoceerr.justrelax.core.database.db.JustRelaxDatabase
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JustRelaxDatabaseTest {

    private val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also {
        JustRelaxDatabase.Schema.create(it)
    }
    private val database = createDatabase(driver)

    @AfterTest
    fun tearDown() = driver.close()

    private fun insertSound(id: String) = database.soundQueries.insertSound(
        id = id,
        names = mapOf("en" to id, "tr" to "ses"),
        categoryId = "RAIN",
        iconUrl = "",
        remoteUrl = "",
        localPath = null,
        isInitial = false,
        sizeBytes = 0,
    )

    private fun insertMixWith(vararg soundIds: String): Long {
        database.savedMixQueries.insertMix("Night", "2026-10-05")
        val mixId = database.savedMixQueries.lastInsertRowId().executeAsOne()
        soundIds.forEach { database.savedMixQueries.insertMixSound(mixId, it, 0.5) }
        return mixId
    }

    @Test
    fun soundNames_roundTripThroughJsonAdapter() {
        insertSound("rain")

        val names = database.soundQueries.selectSoundById("rain").executeAsOne().names

        assertEquals(mapOf("en" to "rain", "tr" to "ses"), names)
    }

    @Test
    fun deletingMix_cascadesToItsSounds() {
        insertSound("rain")
        insertSound("fire")
        val mixId = insertMixWith("rain", "fire")

        database.savedMixQueries.deleteMixById(mixId)

        assertTrue(database.savedMixQueries.selectAllRelations().executeAsList().isEmpty())
    }

    @Test
    fun deletingSound_removesItFromSavedMixes() {
        insertSound("rain")
        insertSound("fire")
        insertMixWith("rain", "fire")

        database.soundQueries.deleteSoundById("rain")

        val remaining = database.savedMixQueries.selectAllRelations().executeAsList().map { it.sound_id }
        assertEquals(listOf("fire"), remaining)
    }
}
