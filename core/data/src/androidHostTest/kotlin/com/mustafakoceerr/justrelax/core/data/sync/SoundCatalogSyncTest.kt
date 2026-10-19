package com.mustafakoceerr.justrelax.core.data.sync

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.data.download.FakeSoundFileStorage
import com.mustafakoceerr.justrelax.core.data.inMemoryDriver
import com.mustafakoceerr.justrelax.core.data.mapper.asSounds
import com.mustafakoceerr.justrelax.core.data.mapper.insert
import com.mustafakoceerr.justrelax.core.data.repository.DatabaseSavedSceneRepository
import com.mustafakoceerr.justrelax.core.database.createDatabase
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.SoundSource
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import com.mustafakoceerr.justrelax.core.testing.testDispatchers
import com.mustafakoceerr.justrelax.core.testing.testSound
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock

class SoundCatalogSyncTest {

    private val driver = inMemoryDriver()
    private val database = createDatabase(driver)
    private val storage = FakeSoundFileStorage()
    private val remote = FakeSoundRemoteDataSource(
        listOf(networkSound("rain"), networkSound("birds", "https://cdn.test/birds_0.ogg", "https://cdn.test/birds_1.ogg"))
    )

    @AfterTest
    fun tearDown() = driver.close()

    
    private suspend fun TestScope.sync() = DatabaseSoundCatalogSync(remote, database, storage, testDispatchers()).syncWithServer()

    private fun TestScope.savedScenes() = DatabaseSavedSceneRepository(database.savedSceneQueries, Clock.System, testDispatchers())

    private fun localSounds(): Map<String, Sound> =
        database.soundQueries.selectSoundsWithClips().executeAsList().asSounds().associateBy { it.id }

    /** Marks every clip of [soundId] as downloaded, with its file in storage. */
    private fun download(soundId: String) {
        database.soundQueries.selectClipsForSound(soundId).executeAsList().forEach { clip ->
            val path = "/sounds/${soundId}_${clip.idx}.ogg"
            database.soundQueries.updateClipLocalPath(path, soundId, clip.idx)
            storage.files += path
        }
    }

    @Test
    fun firstSync_insertsEveryCatalogSound_withItsClips_notDownloaded() = runTest {
        val result = sync()

        assertIs<Resource.Success<Unit>>(result)
        val sounds = localSounds()
        assertEquals(setOf("rain", "birds"), sounds.keys)
        assertEquals(2, sounds.getValue("birds").clips.size)
        assertTrue(sounds.values.none { it.isDownloaded })
    }

    @Test
    fun soundsTheAppCannotUse_areSkipped() = runTest {
        remote.sounds += networkSound("nc").copy(license = "CC_BY_NC_4_0")

        sync()

        assertEquals(setOf("rain", "birds"), localSounds().keys)
    }

    @Test
    fun changedMetadata_isUpdated_keepingDownloadsAndSavedScenes() = runTest {
        sync()
        download("rain")
        savedScenes().save("Night", Scene(listOf(Layer.Bed("rain", 0.4f))))

        remote.sounds = listOf(networkSound("rain").copy(names = mapOf("en" to "Rain")), remote.sounds.last())
        sync()

        val rain = localSounds().getValue("rain")
        assertEquals("Rain", rain.getDisplayName("en"))
        assertTrue(rain.isDownloaded)
        assertEquals(listOf("rain"), savedScenes().getSavedScenes().first().single().scene.layers.map { it.soundId })
    }

    @Test
    fun aClipWhoseFileChanged_mustBeDownloadedAgain() = runTest {
        sync()
        download("birds")

        remote.sounds = listOf(networkSound("birds", "https://cdn.test/birds_0.ogg", "https://cdn.test/birds_1_v2.ogg"))
        sync()

        val clips = localSounds().getValue("birds").clips
        assertEquals("/sounds/birds_0.ogg", clips[0].localPath)
        assertNull(clips[1].localPath)
        assertEquals("https://cdn.test/birds_1_v2.ogg", clips[1].remoteUrl)
        assertEquals(setOf("/sounds/birds_0.ogg"), storage.files)
    }

    @Test
    fun clipsAddedOrRemovedOnTheServer_areAddedOrRemoved() = runTest {
        sync()
        download("birds")

        remote.sounds = listOf(networkSound("birds", "https://cdn.test/birds_0.ogg"))
        sync()
        assertEquals(1, localSounds().getValue("birds").clips.size)
        assertEquals(setOf("/sounds/birds_0.ogg"), storage.files)

        remote.sounds = listOf(networkSound("birds", "https://cdn.test/birds_0.ogg", "https://cdn.test/birds_2.ogg"))
        sync()
        assertEquals(listOf(true, false), localSounds().getValue("birds").clips.map { it.isDownloaded })
    }

    @Test
    fun aRemovedSoundsFile_isDeletedWhereItIsToday_evenIfStoredRelativeOrUnderAnOldContainer() = runTest {
        val storage = FakeSoundFileStorage(appFilesDirectory = "/app/NEW", catalogDirectory = "/app/NEW/audio/catalog")
        DatabaseSoundCatalogSync(remote, database, storage, testDispatchers()).syncWithServer()
        database.soundQueries.updateClipLocalPath("audio/catalog/rain_0.ogg", "rain", 0)
        database.soundQueries.updateClipLocalPath("/app/OLD/audio/catalog/birds_0.ogg", "birds", 0)
        storage.files += setOf("/app/NEW/audio/catalog/rain_0.ogg", "/app/NEW/audio/catalog/birds_0.ogg")

        remote.sounds = emptyList()
        DatabaseSoundCatalogSync(remote, database, storage, testDispatchers()).syncWithServer()

        assertTrue(storage.files.isEmpty(), "left ${storage.files}")
    }

    @Test
    fun aSoundRemovedOnTheServer_isDeleted_withItsFiles_andDropsOutOfSavedScenes() = runTest {
        sync()
        download("rain")
        savedScenes().save("Night", Scene(listOf(Layer.Bed("rain", 0.4f), Layer.Bed("birds", 0.5f))))

        remote.sounds = remote.sounds.filterNot { it.id == "rain" }
        sync()

        assertEquals(setOf("birds"), localSounds().keys)
        assertTrue(storage.files.isEmpty())
        assertEquals(listOf("birds"), savedScenes().getSavedScenes().first().single().scene.layers.map { it.soundId })
    }

    @Test
    fun theUsersOwnAndFreesoundSounds_areNeverTouched() = runTest {
        database.soundQueries.insert(testSound("user_mine", source = SoundSource.USER, localPath = "/mine.m4a"))
        database.soundQueries.insert(testSound("freesound_1", source = SoundSource.FREESOUND, localPath = "/fs.ogg"))

        remote.sounds += networkSound("user_mine").copy(names = mapOf("en" to "Catalog"))
        sync()

        val sounds = localSounds()
        assertEquals(setOf("rain", "birds", "user_mine", "freesound_1"), sounds.keys)
        assertEquals(SoundSource.USER, sounds.getValue("user_mine").source)
        assertEquals("user_mine", sounds.getValue("user_mine").getDisplayName("en"))
        assertTrue(sounds.getValue("freesound_1").isDownloaded)
    }

    @Test
    fun remoteFailure_returnsError_andKeepsLocalData() = runTest {
        sync()
        remote.error = IllegalStateException("offline")

        val result = sync()

        assertIs<Resource.Error>(result)
        assertEquals(setOf("rain", "birds"), localSounds().keys)
    }

    @Test
    fun knownNetworkError_isPassedThroughInsteadOfWrappedAsUnknown() = runTest {
        remote.error = AppError.Network.NoInternet()

        val result = sync()

        assertIs<AppError.Network.NoInternet>(assertIs<Resource.Error>(result).error)
    }
}
