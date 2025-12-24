package com.mustafakoceerr.justrelax.core.ui.util

import coil3.PlatformContext
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUserDomainMask

internal actual fun getDiskCacheDir(context: PlatformContext): Path {
    // iOS'ta PlatformContext genelde kullanılmaz ama imza uyumu için tutuyoruz.
    // NSCachesDirectory: iOS'in cache için ayırdığı, iTunes backup'a girmeyen güvenli alan.
    val paths = NSFileManager.defaultManager.URLsForDirectory(
        directory = NSCachesDirectory,
        inDomains = NSUserDomainMask
    )
    val cacheDirUrl = paths.firstOrNull() as? platform.Foundation.NSURL

    // Eğer yol bulunamazsa fallback olarak tmp kullanılır (Nadir durum)
    return (cacheDirUrl?.path ?: NSTemporaryDirectory()).toPath()
}