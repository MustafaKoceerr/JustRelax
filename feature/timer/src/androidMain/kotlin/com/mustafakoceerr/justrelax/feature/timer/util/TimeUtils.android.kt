package com.mustafakoceerr.justrelax.feature.timer.util

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

actual fun calculateEndTime(timeLeftSeconds: Long): String {
    val now = LocalTime.now()
    val endTime = now.plusSeconds(timeLeftSeconds)

    // Follows the system 12h/24h setting.
    val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    return endTime.format(formatter)
}