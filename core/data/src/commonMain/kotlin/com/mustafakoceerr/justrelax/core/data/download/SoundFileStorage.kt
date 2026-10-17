package com.mustafakoceerr.justrelax.core.data.download

import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/** The app's private directories for sound files. */
internal interface SoundFileStorage {
    suspend fun fileExists(path: String): Boolean
    suspend fun deleteFile(path: String)

    /** Where downloaded catalog clips are kept; created on first use. */
    suspend fun getCatalogDirectoryPath(): String
    suspend fun moveFile(sourcePath: String, destinationPath: String)
}

/**
 * Keeps sound files under `<app files>/audio`. The v1 app kept its downloads in
 * `<app files>/sounds`; that directory is deleted the first time storage is used.
 */
internal class FileSystemSoundFileStorage(
    private val fileSystem: FileSystem,
    private val appFilesDirectory: Path,
    private val dispatchers: DispatcherProvider,
) : SoundFileStorage {

    private val catalogDirectory = appFilesDirectory / AUDIO_DIRECTORY / CATALOG_DIRECTORY
    private val setupLock = Mutex()
    private var isSetUp = false

    override suspend fun fileExists(path: String): Boolean = withContext(dispatchers.io) {
        fileSystem.exists(path.toPath())
    }

    override suspend fun deleteFile(path: String) = withContext(dispatchers.io) {
        fileSystem.delete(path.toPath())
    }

    override suspend fun getCatalogDirectoryPath(): String = withContext(dispatchers.io) {
        setupLock.withLock {
            if (!isSetUp) {
                fileSystem.deleteRecursively(appFilesDirectory / LEGACY_SOUNDS_DIRECTORY)
                fileSystem.createDirectories(catalogDirectory)
                isSetUp = true
            }
        }
        catalogDirectory.toString()
    }

    override suspend fun moveFile(sourcePath: String, destinationPath: String) = withContext(dispatchers.io) {
        fileSystem.atomicMove(sourcePath.toPath(), destinationPath.toPath())
    }

    private companion object {
        const val AUDIO_DIRECTORY = "audio"
        const val CATALOG_DIRECTORY = "catalog"
        const val LEGACY_SOUNDS_DIRECTORY = "sounds"
    }
}
