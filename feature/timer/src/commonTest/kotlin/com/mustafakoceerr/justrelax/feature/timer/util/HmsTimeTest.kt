package com.mustafakoceerr.justrelax.feature.timer.util

import kotlin.test.Test
import kotlin.test.assertEquals

class HmsTimeTest {

    @Test
    fun splitsSecondsIntoHoursMinutesAndSeconds() {
        assertEquals(HmsTime(1, 30, 5), HmsTime.fromSeconds(5_405))
        assertEquals(HmsTime(0, 0, 0), HmsTime.fromSeconds(0))
    }

    @Test
    fun aNegativeTimeIsZero() {
        assertEquals(HmsTime(0, 0, 0), HmsTime.fromSeconds(-30))
    }

    @Test
    fun totalSecondsAddsTheUnitsBack() {
        assertEquals(5_405L, HmsTime(1, 30, 5).totalSeconds)
    }

    @Test
    fun twoDigitsPadsWithAZero() {
        assertEquals("07", 7.twoDigits())
        assertEquals("42", 42.twoDigits())
    }
}
