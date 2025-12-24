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
            // YENİ: Platforma özel, kalıcı cache dizinini alıyoruz.
            // Bu dizin OS tarafından yönetilir ve uygulama silinmedikçe (veya yer dolmadıkça) kalıcıdır.
            val cacheDir = getDiskCacheDir(context)

            DiskCache.Builder()
                .directory(cacheDir.resolve("image_cache")) // Alt klasör oluştur
                .maxSizeBytes(30L * 1024 * 1024) // 100 MB Limit (Ambiyans ikonları için bol bol yeter)
                .build()
        }
        .crossfade(true)
        .build()
}