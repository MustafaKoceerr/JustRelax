package com.mustafakoceerr.justrelax.core.ui.util

import coil3.PlatformContext
import okio.Path

/**
 * Platforma özel Cache dizinini döndüren yardımcı fonksiyon.
 * Coil ImageLoader konfigürasyonu için kullanılır.
 */
internal expect fun getDiskCacheDir(context: PlatformContext): Path