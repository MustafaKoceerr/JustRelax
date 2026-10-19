package com.mustafakoceerr.justrelax.core.ui.util

/** Time left as `m:ss`, or `h:mm:ss` from an hour up, for a countdown shown in a small space. */
fun formatCountdown(seconds: Long): String {
    val total = seconds.coerceAtLeast(0)
    val hours = total / 3_600
    val minutes = total % 3_600 / 60
    val secs = (total % 60).toString().padStart(2, '0')
    return if (hours > 0) "$hours:${minutes.toString().padStart(2, '0')}:$secs" else "$minutes:$secs"
}
