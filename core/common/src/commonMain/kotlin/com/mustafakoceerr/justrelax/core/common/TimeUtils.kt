package com.mustafakoceerr.justrelax.core.common

import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

fun formatTime(totalSeconds: Long): String {
    // 1. Matematiği yapalım
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    // 2. Formatlama (Saf Kotlin - KMM Uyumlu)
    // padStart(2, '0') -> Sayı tek haneliyle başına 0 koyar (5 -> 05)
    val hh = hours.toString().padStart(2, '0')
    val mm = minutes.toString().padStart(2, '0')
    val ss = seconds.toString().padStart(2, '0')

    // 3. Görseldeki gibi aralıklı dönüş
    return "$hh : $mm : $ss"
}


// Tarih formatlama logic'i (Helper)
@OptIn(ExperimentalTime::class)
private fun formatEpoch(epochMillis: Long): String {
    val instant = Instant.fromEpochMilliseconds(epochMillis)
    val date = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    // Örn: 12.10.2025
    return "${date.day}.${date.month.number}.${date.year}"
}