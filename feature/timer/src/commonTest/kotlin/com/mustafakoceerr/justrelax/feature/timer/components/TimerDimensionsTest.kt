package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.util.WindowWidthSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TimerDimensionsTest {

    @Test
    fun aShortWindowGetsTheShortSizesWhateverItsWidth() {
        val sizes = WindowWidthSize.entries.map { TimerDimensions.forWindow(it, isCompactHeight = true) }

        assertEquals(1, sizes.toSet().size)
    }

    @Test
    fun theWheelsFitAPhoneHeldSideways() {
        // About 216dp is left under the status and top bars of a 360dp-high window.
        val wheels = TimerDimensions.forWindow(WindowWidthSize.MEDIUM, isCompactHeight = true).wheelItemHeight * 3

        assertTrue(wheels + LABEL_HEIGHT <= 216.dp, "wheels take $wheels")
    }

    @Test
    fun aTallWindowStillSizesByWidth() {
        val compact = TimerDimensions.forWindow(WindowWidthSize.COMPACT, isCompactHeight = false)
        val expanded = TimerDimensions.forWindow(WindowWidthSize.EXPANDED, isCompactHeight = false)

        assertTrue(expanded.wheelItemHeight > compact.wheelItemHeight)
    }

    private companion object {
        val LABEL_HEIGHT = 28.dp
    }
}
