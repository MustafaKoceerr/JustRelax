package com.mustafakoceerr.justrelax.core.presentation.util

import kotlin.test.Test
import kotlin.test.assertEquals

class DragVolumeTest {

    @Test
    fun dragging_movesTheVolumeByTheShareOfTheWidth() {
        assertEquals(0.75f, volumeAfterDrag(start = 0.5f, dragX = 50f, width = 200f))
        assertEquals(0.25f, volumeAfterDrag(start = 0.5f, dragX = -50f, width = 200f))
    }

    @Test
    fun theVolume_staysBetweenZeroAndOne() {
        assertEquals(1f, volumeAfterDrag(start = 0.9f, dragX = 500f, width = 200f))
        assertEquals(0f, volumeAfterDrag(start = 0.1f, dragX = -500f, width = 200f))
    }

    @Test
    fun withoutWidth_theVolumeStays() {
        assertEquals(0.4f, volumeAfterDrag(start = 0.4f, dragX = 30f, width = 0f))
    }
}
