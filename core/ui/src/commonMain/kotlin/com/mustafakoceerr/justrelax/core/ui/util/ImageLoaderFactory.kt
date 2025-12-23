package com.mustafakoceerr.justrelax.core.ui.util

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import okio.FileSystem
import okio.Path.Companion.toPath

fun getAsyncImageLoader(context: PlatformContext): ImageLoader {
    return ImageLoader.Builder(context)
        .components {
            add(KtorNetworkFetcherFactory())
            add(SvgDecoder.Factory())
        }
        .diskCache {
            // Cache klasörünü belirle
            // Not: SYSTEM_TEMPORARY_DIRECTORY yerine uygulamanın cache dizinini kullanmak daha güvenli olabilir
            // ama KMP'de standart yol budur.
            DiskCache.Builder()
                .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "image_cache".toPath())
                .maxSizeBytes(50L * 1024 * 1024) // 50 MB
                .build()
        }
        // .respectCacheHeaders(false) <-- BU SATIRI SİLİYORUZ (Coil 3'te yok)
        .crossfade(true)
        .build()
}