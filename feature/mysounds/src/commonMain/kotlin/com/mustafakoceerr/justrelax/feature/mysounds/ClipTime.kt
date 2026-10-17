package com.mustafakoceerr.justrelax.feature.mysounds

/** A clip position or length as `m:ss.t`, precise enough for trimming by hand. */
fun formatClipTime(ms: Long): String {
    val tenths = ms / 100
    val minutes = tenths / 600
    val seconds = tenths / 10 % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}.${tenths % 10}"
}
