package com.mustafakoceerr.justrelax.core.data.download

import com.mustafakoceerr.justrelax.core.testing.testDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FileSystemSoundFileStorageTest {

    private val fileSystem = FakeFileSystem()
    private val appFiles = "/data/app/files".toPath()

    private fun TestScope.storage() = FileSystemSoundFileStorage(
        fileSystem = fileSystem,
        appFilesDirectory = appFiles,
        dispatchers = testDispatchers(),
    )

    @Test
    fun catalogClips_liveInTheirOwnDirectory() = runTest {
        val directory = storage().getCatalogDirectoryPath()

        assertEquals("/data/app/files/audio/catalog", directory)
        assertTrue(fileSystem.metadata(directory.toPath()).isDirectory)
    }

    @Test
    fun theV1SoundFiles_areDeleted() = runTest {
        val v1Sounds = appFiles / "sounds"
        fileSystem.createDirectories(v1Sounds)
        fileSystem.write(v1Sounds / "rain.mp3") { writeUtf8("audio") }
        fileSystem.write(appFiles / "justrelax_settings.preferences_pb") { writeUtf8("settings") }

        storage().getCatalogDirectoryPath()

        assertFalse(fileSystem.exists(v1Sounds))
        assertTrue(fileSystem.exists(appFiles / "justrelax_settings.preferences_pb"))
    }

    @Test
    fun aFile_canBeMovedIntoPlace_andDeleted() = runTest {
        val storage = storage()
        val directory = storage.getCatalogDirectoryPath()
        fileSystem.write("$directory/rain_0.ogg.tmp".toPath()) { writeUtf8("audio") }

        storage.moveFile("$directory/rain_0.ogg.tmp", "$directory/rain_0.ogg")
        assertTrue(storage.fileExists("$directory/rain_0.ogg"))

        storage.deleteFile("$directory/rain_0.ogg")
        assertFalse(storage.fileExists("$directory/rain_0.ogg"))
    }
}
