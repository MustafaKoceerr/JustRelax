package com.mustafakoceerr.justrelax.core.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.mustafakoceerr.justrelax.core.database.db.JustRelaxDatabase
import com.mustafakoceerr.justrelax.core.model.License
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.SoundIcon
import com.mustafakoceerr.justrelax.core.model.SoundSource
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JustRelaxDatabaseTest {

    private val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also {
        JustRelaxDatabase.Schema.create(it)
    }
    private val database = createDatabase(driver)
    private val sounds get() = database.soundQueries
    private val savedScenes get() = database.savedSceneQueries

    @AfterTest
    fun tearDown() = driver.close()

    private fun insertSound(id: String, kind: SoundKind = SoundKind.BED, clipCount: Int = 1) {
        sounds.insertSound(
            id = id,
            source = SoundSource.BUILT_IN,
            remote_id = null,
            names = mapOf("en" to id, "tr" to "ses"),
            category = SoundCategory.NATURE,
            icon = SoundIcon.BIRD,
            kind = kind,
            tags = listOf("forest", "morning"),
            default_frequency = EventFrequency.OFTEN,
            license = License.CC_BY_4_0,
            attribution_title = "Dawn chorus",
            attribution_author = "someone",
            attribution_url = "https://freesound.org/s/1",
            is_initial = true,
        )
        repeat(clipCount) { index ->
            sounds.insertClip(id, index.toLong(), "https://cdn.test/$id-$index.ogg", null, 2_000, 10_000)
        }
    }

    private fun saveSceneWith(vararg soundIds: String): Long {
        savedScenes.insertScene("Night", 1_000)
        val sceneId = savedScenes.lastInsertRowId().executeAsOne()
        soundIds.forEachIndexed { position, soundId ->
            savedScenes.insertLayer(sceneId, soundId, position.toLong(), 0.5, EventFrequency.RARE)
        }
        return sceneId
    }

    @Test
    fun aSound_roundTripsThroughTheColumnAdapters() {
        insertSound("birds", kind = SoundKind.EVENT)

        val sound = sounds.selectSoundById("birds").executeAsOne()

        assertEquals(mapOf("en" to "birds", "tr" to "ses"), sound.names)
        assertEquals(listOf("forest", "morning"), sound.tags)
        assertEquals(SoundKind.EVENT, sound.kind)
        assertEquals(SoundCategory.NATURE, sound.category)
        assertEquals(SoundIcon.BIRD, sound.icon)
        assertEquals(EventFrequency.OFTEN, sound.default_frequency)
        assertEquals(License.CC_BY_4_0, sound.license)
        assertEquals(SoundSource.BUILT_IN, sound.source)
    }

    @Test
    fun clips_comeBackInOrder() {
        insertSound("birds", clipCount = 3)

        val clips = sounds.selectClipsForSound("birds").executeAsList()

        assertEquals(listOf(0L, 1L, 2L), clips.map { it.idx })
        assertNull(clips.first().local_path)
    }

    @Test
    fun deletingASound_deletesItsClips() {
        insertSound("birds", clipCount = 3)
        insertSound("rain")

        sounds.deleteSoundById("birds")

        assertEquals(listOf("rain"), sounds.selectAllClips().executeAsList().map { it.sound_id })
    }

    @Test
    fun deletingASavedScene_deletesItsLayers() {
        insertSound("rain")
        insertSound("birds")
        val sceneId = saveSceneWith("rain", "birds")

        savedScenes.deleteSceneById(sceneId)

        assertTrue(savedScenes.selectAllLayers().executeAsList().isEmpty())
    }

    @Test
    fun deletingASound_removesItFromSavedScenes() {
        insertSound("rain")
        insertSound("birds")
        saveSceneWith("rain", "birds")

        sounds.deleteSoundById("rain")

        val remaining = savedScenes.selectAllLayers().executeAsList()
        assertEquals(listOf("birds"), remaining.map { it.sound_id })
        assertEquals(EventFrequency.RARE, remaining.single().frequency)
    }

    @Test
    fun updatingASound_keepsItsClipsAndSavedScenes() {
        insertSound("rain")
        saveSceneWith("rain")

        sounds.updateSoundMetadata(
            names = mapOf("en" to "Rain"),
            category = SoundCategory.RAIN,
            icon = SoundIcon.RAIN,
            kind = SoundKind.BED,
            tags = emptyList(),
            default_frequency = EventFrequency.NORMAL,
            license = License.CC0,
            attribution_title = null,
            attribution_author = null,
            attribution_url = null,
            is_initial = false,
            id = "rain",
        )

        assertEquals(1, sounds.selectClipsForSound("rain").executeAsList().size)
        assertEquals(1, savedScenes.selectAllLayers().executeAsList().size)
    }
}
