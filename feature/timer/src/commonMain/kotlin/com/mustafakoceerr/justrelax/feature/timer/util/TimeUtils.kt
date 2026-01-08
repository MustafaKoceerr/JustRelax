package com.mustafakoceerr.justrelax.feature.timer.util

expect fun calculateEndTime(timeLeftSeconds: Long): String

internal fun Long.toFormattedTime(): String {
    if (this < 0) return "00:00"

    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60

    fun Long.pad(): String = this.toString().padStart(2, '0')

    return if (hours > 0) {
        "${hours.pad()}:${minutes.pad()}:${seconds.pad()}"
    } else {
        "${minutes.pad()}:${seconds.pad()}"
    }
}