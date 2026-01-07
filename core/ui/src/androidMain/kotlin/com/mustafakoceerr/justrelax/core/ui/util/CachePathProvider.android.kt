package com.mustafakoceerr.justrelax.core.ui.util

import coil3.PlatformContext
import okio.Path
import okio.Path.Companion.toPath

internal actual fun getDiskCacheDir(context: PlatformContext): Path {
    return context.cacheDir.absolutePath.toPath()
}