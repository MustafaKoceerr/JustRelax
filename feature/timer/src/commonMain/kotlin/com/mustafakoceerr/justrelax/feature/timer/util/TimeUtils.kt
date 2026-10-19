package com.mustafakoceerr.justrelax.feature.timer.util

fun Long.toFormattedTime(): String {
    if (this < 0) return "00 : 00"
    val (hours, minutes, seconds) = HmsTime.fromSeconds(this)
    return if (hours > 0) {
        "${hours.twoDigits()} : ${minutes.twoDigits()} : ${seconds.twoDigits()}"
    } else {
        "${minutes.twoDigits()} : ${seconds.twoDigits()}"
    }
}
