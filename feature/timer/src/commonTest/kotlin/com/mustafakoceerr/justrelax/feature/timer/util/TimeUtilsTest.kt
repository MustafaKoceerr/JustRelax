package com.mustafakoceerr.justrelax.feature.timer.util

import kotlin.test.Test
import kotlin.test.assertEquals

class TimeUtilsTest {

    @Test
    fun underAnHourShowsMinutesAndSeconds() {
        assertEquals("00 : 00", 0L.toFormattedTime())
        assertEquals("05 : 09", 309L.toFormattedTime())
        assertEquals("59 : 59", 3_599L.toFormattedTime())
    }

    @Test
    fun anHourOrMoreAlsoShowsHours() {
        assertEquals("01 : 00 : 00", 3_600L.toFormattedTime())
        assertEquals("12 : 34 : 56", 45_296L.toFormattedTime())
    }

    @Test
    fun aNegativeTimeShowsZero() {
        assertEquals("00 : 00", (-1L).toFormattedTime())
    }
}
