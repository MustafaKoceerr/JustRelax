package com.mustafakoceerr.justrelax.core.ui.util

import coil3.PlatformContext
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUserDomainMask

internal actual fun getDiskCacheDir(context: PlatformContext): Path {
    val paths = NSFileManager.defaultManager.URLsForDirectory(
        directory = NSCachesDirectory,
        inDomains = NSUserDomainMask
    )
    val cacheDirUrl = paths.firstOrNull() as? platform.Foundation.NSURL
    return (cacheDirUrl?.path ?: NSTemporaryDirectory()).toPath()
}