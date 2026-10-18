package com.mustafakoceerr.justrelax.feature.saved.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/** Formats [instant] in [timeZone] (the device's by default) with a pattern made of dd, MM and yyyy. */
fun formatDate(
    instant: Instant,
    formatPattern: String,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val localDateTime = instant.toLocalDateTime(timeZone)

    val day = localDateTime.day.toString().padStart(2, '0')
    val month = localDateTime.month.number.toString().padStart(2, '0')
    val year = localDateTime.year.toString()

    return formatPattern
        .replace("dd", day, ignoreCase = true)
        .replace("MM", month, ignoreCase = true)
        .replace("yyyy", year, ignoreCase = true)
}
