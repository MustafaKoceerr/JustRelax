package com.mustafakoceerr.justrelax.core.data.download

import android.content.res.AssetManager
import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.IOException
import okio.Path.Companion.toPath
import okio.source
import okio.use

/**
 * Copies `asset:///` URLs out of the APK (the debug build's sound pack) and downloads every other
 * URL over the network.
 */
internal class AndroidFileDownloader(
    private val assets: AssetManager,
    private val fileSystem: FileSystem,
    private val dispatchers: DispatcherProvider,
    private val network: FileDownloader,
) : FileDownloader {

    override suspend fun downloadFile(url: String, destinationPath: String): Boolean {
        if (!url.startsWith(ASSET_URL_PREFIX)) return network.downloadFile(url, destinationPath)

        return withContext(dispatchers.io) {
            val destination = destinationPath.toPath()
            try {
                assets.open(url.removePrefix(ASSET_URL_PREFIX)).source().use { source ->
                    fileSystem.write(destination) { writeAll(source) }
                }
                true
            } catch (e: IOException) {
                runCatching { fileSystem.delete(destination) }
                false
            }
        }
    }

    private companion object {
        const val ASSET_URL_PREFIX = "asset:///"
    }
}
