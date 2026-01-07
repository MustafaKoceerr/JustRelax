package com.mustafakoceerr.justrelax.core.ui.util

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import coil3.svg.SvgDecoder

fun getAsyncImageLoader(context: PlatformContext): ImageLoader {
    return ImageLoader.Builder(context)
        .components {
            add(KtorNetworkFetcherFactory())
            add(SvgDecoder.Factory())
        }
        .diskCache {
            val cacheDir = getDiskCacheDir(context)
            DiskCache.Builder()
                .directory(cacheDir.resolve("image_cache"))
                .maxSizeBytes(30L * 1024 * 1024) // 30 MB
                .build()
        }
        .crossfade(true)
        .build()
}