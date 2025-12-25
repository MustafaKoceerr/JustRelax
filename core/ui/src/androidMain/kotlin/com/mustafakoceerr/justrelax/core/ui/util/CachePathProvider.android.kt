package com.mustafakoceerr.justrelax.core.ui.util

import coil3.PlatformContext
import okio.Path
import okio.Path.Companion.toPath

internal actual fun getDiskCacheDir(context: PlatformContext): Path {
    // Android'de PlatformContext = android.content.Context
    // Uygulamanın kalıcı cache dizinini döner (/data/user/0/com.app/cache)
    return context.cacheDir.absolutePath.toPath()
}