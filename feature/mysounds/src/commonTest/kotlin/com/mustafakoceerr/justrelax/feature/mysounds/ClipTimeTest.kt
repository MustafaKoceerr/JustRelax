package com.mustafakoceerr.justrelax.feature.mysounds

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
}
