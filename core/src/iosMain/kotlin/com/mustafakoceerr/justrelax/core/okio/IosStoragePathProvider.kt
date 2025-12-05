package com.mustafakoceerr.justrelax.core.okio

import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

class IosStoragePathProvider : StoragePathProvider {
    override fun getAppDataDir(): Path {
        val paths = NSFileManager.defaultManager.URLsForDirectory(
            directory = NSDocumentDirectory,
            inDomains = NSUserDomainMask
        )
        val documentsURL = paths.first() as? platform.Foundation.NSURL
        // String?.toPath() olmaz, o yüzden boş string kontrolü yapıyoruz
        return (documentsURL?.path ?: "").toPath()
    }
}