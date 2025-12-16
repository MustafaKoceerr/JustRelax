package com.mustafakoceerr.justrelax.feature.timer.util

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// Android tarafı Java kütüphanelerini kullanabilir
actual fun calculateEndTime(timeLeftSeconds: Long): String {
    val now = LocalTime.now()
    val endTime = now.plusSeconds(timeLeftSeconds)

    // Android sistem ayarına göre (12h/24h) formatlar
    val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    return endTime.format(formatter)
}