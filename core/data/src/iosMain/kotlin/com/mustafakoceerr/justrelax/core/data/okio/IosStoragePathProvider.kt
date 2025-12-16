package com.mustafakoceerr.justrelax.core.data.okio

import com.mustafakoceerr.justrelax.core.domain.manager.StoragePathProvider
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

class IosStoragePathProvider : StoragePathProvider {
    override fun getAppDataDir(): Path {
        val paths = NSFileManager.defaultManager.URLsForDirectory(
            directory = NSDocumentDirectory,
            inDomains = NSUserDomainMask
        )
        val documentsURL = paths.first() as? NSURL
        // String?.toPath() olmaz, o yüzden boş string kontrolü yapıyoruz
        return (documentsURL?.path ?: "").toPath()
    }

    override fun getCacheDir(): Path {
        val paths = NSFileManager.defaultManager.URLsForDirectory(
            directory = NSCachesDirectory, // <-- Değişiklik burada
            inDomains = NSUserDomainMask
        )
        val cacheUrl = paths.first() as? NSURL
        return (cacheUrl?.path ?: "").toPath()
    }
}