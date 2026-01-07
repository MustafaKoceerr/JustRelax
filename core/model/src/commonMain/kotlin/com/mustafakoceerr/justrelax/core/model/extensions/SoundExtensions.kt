package com.mustafakoceerr.justrelax.core.model.extensions

import com.mustafakoceerr.justrelax.core.model.Sound

fun List<Sound>.calculateTotalSizeInMb(): Float {
    if (this.isEmpty()) return 0f
    val totalBytes = this.sumOf { it.sizeBytes }
    return totalBytes / (1024f * 1024f)
}