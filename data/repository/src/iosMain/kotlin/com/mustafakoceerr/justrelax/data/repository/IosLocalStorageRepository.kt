package com.mustafakoceerr.justrelax.data.repository

import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import com.mustafakoceerr.justrelax.core.domain.repository.system.LocalStorageRepository
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * iOS için 'LocalStorageRepository' implementasyonu.
 */
@OptIn(ExperimentalForeignApi::class)
internal class IosLocalStorageRepository(
    private val dispatchers: DispatcherProvider
) : LocalStorageRepository {

    // iOS'te uygulamanın veri saklayabileceği 'Documents' klasörünü alıyoruz.
    private val appDataDirectory: Path by lazy {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true, // Klasör yoksa oluştur.
            error = null,
        )
        requireNotNull(documentDirectory?.path).toPath()
    }

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

    /**
     * YENİ: Bir dosyayı atomik olarak taşır.
     */
    override suspend fun moveFile(sourcePath: String, destinationPath: String) =
        withContext(dispatchers.io) {
            FileSystem.SYSTEM.atomicMove(sourcePath.toPath(), destinationPath.toPath())
        }
}
