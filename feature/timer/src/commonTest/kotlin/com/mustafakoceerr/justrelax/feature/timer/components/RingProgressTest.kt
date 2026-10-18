package com.mustafakoceerr.justrelax.feature.timer.components

import kotlin.test.Test
import kotlin.test.assertEquals

class RingProgressTest {

    @Test
    fun isTheShareOfTimeLeft() {
        assertEquals(0.25f, ringProgress(remainingSeconds = 15, totalSeconds = 60))
    }

    @Test
    fun staysBetweenEmptyAndFull() {
        assertEquals(0f, ringProgress(remainingSeconds = 10, totalSeconds = 0))
        assertEquals(1f, ringProgress(remainingSeconds = 90, totalSeconds = 60))
        assertEquals(0f, ringProgress(remainingSeconds = -1, totalSeconds = 60))
    }
}
