package com.mustafakoceerr.justrelax.feature.timer.util

/** A length of time split the way the timer shows it. */
data class HmsTime(val hours: Int, val minutes: Int, val seconds: Int) {

    val totalSeconds: Long
        get() = hours * 3600L + minutes * 60L + seconds

    companion object {
        /** Negative lengths are zero. */
        fun fromSeconds(totalSeconds: Long): HmsTime {
            val total = totalSeconds.coerceAtLeast(0)
            return HmsTime(
                hours = (total / 3600).toInt(),
                minutes = (total % 3600 / 60).toInt(),
                seconds = (total % 60).toInt(),
            )
        }
    }
}

fun Int.twoDigits(): String = toString().padStart(2, '0')
