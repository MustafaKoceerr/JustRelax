package com.mustafakoceerr.justrelax.core.presentation.util

import kotlin.test.Test
import kotlin.test.assertEquals

class CountdownTest {

    @Test
    fun underAnHour_isMinutesAndSeconds() {
        assertEquals("0:00", formatCountdown(0))
        assertEquals("0:09", formatCountdown(9))
        assertEquals("24:10", formatCountdown(24 * 60 + 10))
    }

    @Test
    fun fromAnHour_addsTheHours() {
        assertEquals("1:00:00", formatCountdown(3_600))
        assertEquals("2:05:07", formatCountdown(2 * 3_600 + 5 * 60 + 7))
    }

    @Test
    fun negativeTime_isZero() {
        assertEquals("0:00", formatCountdown(-5))
    }
}
