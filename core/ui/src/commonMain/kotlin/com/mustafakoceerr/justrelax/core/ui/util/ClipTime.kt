package com.mustafakoceerr.justrelax.core.ui.util

/** A clip position or length as `m:ss.t`, precise enough for trimming by hand. */
fun formatClipTime(ms: Long): String {
    val tenths = ms / 100
    val minutes = tenths / 600
    val seconds = tenths / 10 % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}.${tenths % 10}"
}

/** A clip's length as `m:ss`, rounded to the nearest second. */
fun formatClipLength(ms: Long): String {
    val seconds = (ms + 500) / 1_000
    return "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
}
