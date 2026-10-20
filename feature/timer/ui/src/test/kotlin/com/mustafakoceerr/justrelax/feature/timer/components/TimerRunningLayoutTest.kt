package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.util.WindowWidthSize
import com.mustafakoceerr.justrelax.feature.timer.TimerUiState
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
// Big enough for every window the tests lay out; each test sizes its own box.
@Config(sdk = [35], qualifiers = "w1400dp-h900dp")
class TimerRunningLayoutTest {

    @get:Rule
    val compose = createComposeRule()

    private val running = TimerUiState(isSetupMode = false, totalSeconds = 2_700, remainingSeconds = 2_683)

    @Test
    fun theRingFitsATabletHeldSideways() {
        showRunningTimer(width = 1280.dp, height = 700.dp, WindowWidthSize.EXPANDED)

        val ring = compose.onNodeWithTag(TIMER_RING_TAG).getBoundsInRoot()
        assertTrue(ring.bottom <= 700.dp, "the ring ends at ${ring.bottom}")
        assertTrue(ring.right - ring.left <= 450.dp, "the ring is ${ring.right - ring.left} wide")
    }

    @Test
    fun theRingStillFillsAPhone() {
        showRunningTimer(width = 412.dp, height = 800.dp, WindowWidthSize.COMPACT)

        val ring = compose.onNodeWithTag(TIMER_RING_TAG).getBoundsInRoot()
        // 85% of the width left inside the 16dp padding.
        assertTrue(ring.right - ring.left >= 320.dp, "the ring is ${ring.right - ring.left} wide")
    }

    private fun showRunningTimer(width: Dp, height: Dp, windowWidth: WindowWidthSize) {
        compose.setContent {
            Box(Modifier.size(width, height)) {
                TimerRunningLayout(
                    uiState = running,
                    dimensions = TimerDimensions.forWindow(windowWidth, isCompactHeight = false),
                    isCompactHeight = false,
                    onToggleClick = {},
                    onCancelClick = {},
                )
            }
        }
    }
}
