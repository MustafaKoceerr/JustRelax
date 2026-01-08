package com.mustafakoceerr.justrelax.feature.saved.util

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalTime::class)
fun formatIsoDate(isoString: String, formatPattern: String): String {
    return try {
        val instant = Instant.parse(isoString)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        val day = localDateTime.day.toString().padStart(2, '0')
        val month = localDateTime.month.number.toString().padStart(2, '0')
        val year = localDateTime.year.toString()

        formatPattern
            .replace("dd", day, ignoreCase = true)
            .replace("MM", month, ignoreCase = true)
            .replace("yyyy", year, ignoreCase = true)

    } catch (e: Exception) {
        isoString.split("T").firstOrNull() ?: isoString
    }
}