package com.mustafakoceerr.justrelax.data.repository

import android.content.Context
import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.domain.repository.system.LocalStorageRepository
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

internal class AndroidLocalStorageRepository(
    context: Context,
    private val dispatchers: DispatcherProvider
) : LocalStorageRepository {

    private val appDataDirectory: Path = context.filesDir.path.toPath()

    override suspend fun fileExists(path: String): Boolean = withContext(dispatchers.io) {
        FileSystem.SYSTEM.exists(path.toPath())
    }

    override suspend fun deleteFile(path: String) = withContext(dispatchers.io) {
        FileSystem.SYSTEM.delete(path.toPath())
    }

    override fun getSoundsDirectoryPath(): String {
        val soundsDir = appDataDirectory / DataConstants.SOUNDS_DIRECTORY_NAME
        if (!FileSystem.SYSTEM.exists(soundsDir)) {
            FileSystem.SYSTEM.createDirectory(soundsDir)
        }
        return soundsDir.toString()
    }

    override suspend fun moveFile(sourcePath: String, destinationPath: String) = withContext(dispatchers.io) {
        FileSystem.SYSTEM.atomicMove(sourcePath.toPath(), destinationPath.toPath())
    }
}