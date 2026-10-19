package com.mustafakoceerr.justrelax.core.media

import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.withContext
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * Copies a file from the document picker into `Caches/import`. The picker hands over a temporary
 * copy, which iOS may delete, so the editor works on its own.
 */
@OptIn(ExperimentalForeignApi::class)
internal class IosAudioImporter(
    private val dispatchers: DispatcherProvider,
    private val cacheDirectory: String = defaultCacheDirectory(),
) : AudioImporter {

    private val fileManager = NSFileManager.defaultManager

    override suspend fun import(uri: String): ImportedAudio = withContext(dispatchers.io) {
        val source = NSURL.URLWithString(uri)?.path ?: throw MediaException("Not a file: $uri")
        val size = (fileManager.attributesOfItemAtPath(source, error = null)?.get(NSFileSize) as? NSNumber)?.longLongValue
            ?: throw MediaException("Cannot read $uri")
        if (size > MAX_BYTES) throw MediaException("Larger than ${MAX_BYTES / 1_048_576} MB")

        // The extension stays: AVAudioFile tells MP3, AAC and the rest apart by it.
        val extension = NSURL.fileURLWithPath(source).pathExtension.orEmpty()
        val directory = "$cacheDirectory/$IMPORT_DIRECTORY"
        val copy = "$directory/$IMPORT_FILE" + if (extension.isEmpty()) "" else ".$extension"
        // Only the latest copy is kept.
        fileManager.removeItemAtPath(directory, error = null)
        fileManager.createDirectoryAtPath(directory, withIntermediateDirectories = true, attributes = null, error = null)
        withNSError("Cannot store the copy") { error -> if (fileManager.copyItemAtPath(source, toPath = copy, error = error)) true else null }
        ImportedAudio(copy, NSURL.fileURLWithPath(source).lastPathComponent)
    }

    private companion object {
        const val IMPORT_DIRECTORY = "import"
        const val IMPORT_FILE = "audio"
        const val MAX_BYTES = 200L * 1_048_576

        fun defaultCacheDirectory(): String =
            NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true).first() as String
    }
}
