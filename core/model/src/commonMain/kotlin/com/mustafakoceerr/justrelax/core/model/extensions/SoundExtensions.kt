package com.mustafakoceerr.justrelax.core.model.extensions

import com.mustafakoceerr.justrelax.core.model.Sound

/**
 * Bir ses listesinin toplam boyutunu MB (Megabyte) cinsinden hesaplar.
 */
fun List<Sound>.calculateTotalSizeInMb(): Float {
    if (this.isEmpty()) return 0f

    val totalBytes = this.sumOf { it.sizeBytes }
    // Bytes -> MB (1024 * 1024)
    return totalBytes / (1024f * 1024f)
}