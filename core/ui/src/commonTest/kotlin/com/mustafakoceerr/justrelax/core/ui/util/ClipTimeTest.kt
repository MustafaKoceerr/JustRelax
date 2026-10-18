package com.mustafakoceerr.justrelax.core.ui.util

import kotlin.test.Test
import kotlin.test.assertEquals

class ClipTimeTest {

    @Test
    fun timesShowMinutesSecondsAndTenths() {
        assertEquals("0:00.0", formatClipTime(0))
        assertEquals("0:04.2", formatClipTime(4_249))
        assertEquals("1:05.0", formatClipTime(65_000))
        assertEquals("12:00.9", formatClipTime(720_999))
    }

    @Test
    fun lengthsRoundToWholeSeconds() {
        assertEquals("0:04", formatClipLength(4_499))
        assertEquals("0:05", formatClipLength(4_500))
        assertEquals("2:00", formatClipLength(119_600))
    }
}
