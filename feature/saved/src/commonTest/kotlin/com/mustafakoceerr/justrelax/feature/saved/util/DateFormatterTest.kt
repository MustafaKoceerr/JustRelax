package com.mustafakoceerr.justrelax.feature.saved.util

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class DateFormatterTest {

    // 2026-03-05T23:30:00Z
    private val instant = Instant.fromEpochSeconds(1_772_753_400)

    @Test
    fun everyLanguagesPatternIsFilledWithPaddedNumbers() {
        assertEquals("03/05/2026", formatDate(instant, "MM/dd/yyyy", TimeZone.UTC))
        assertEquals("05.03.2026", formatDate(instant, "dd.MM.yyyy", TimeZone.UTC))
        assertEquals("2026-03-05", formatDate(instant, "yyyy-MM-dd", TimeZone.UTC))
    }

    @Test
    fun theDateIsTheOneInTheGivenTimeZone() {
        assertEquals("06.03.2026", formatDate(instant, "dd.MM.yyyy", TimeZone.of("Europe/Istanbul")))
    }
}
