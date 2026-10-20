package com.mustafakoceerr.justrelax.feature.mysounds.components

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.mustafakoceerr.justrelax.core.media.TrimRange
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The handles reach the screen edges, where gesture navigation's back swipe starts: the trimmer
 * has to keep those drags for itself or the editor closes mid-trim.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WaveformTrimmerTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun theBackGestureLeavesTheWholeTrimmerAlone() {
        compose.setContent {
            WaveformTrimmer(
                peaks = List(60) { 0.5f },
                sourceMs = 60_000,
                range = TrimRange(0, 60_000),
                onMoveStart = {},
                onMoveEnd = {},
                modifier = Modifier.testTag(TRIMMER),
            )
        }
        compose.waitForIdle()

        val bounds = compose.onNodeWithTag(TRIMMER).fetchSemanticsNode().boundsInWindow
        val trimmer = Rect(bounds.left.roundToInt(), bounds.top.roundToInt(), bounds.right.roundToInt(), bounds.bottom.roundToInt())
        val excluded = buildList { collect(compose.activity.window.decorView, this) }
        assertTrue(excluded.any { it.contains(trimmer) }, "trimmer $trimmer, excluded $excluded")
    }

    /** Every rect a view keeps from system gestures, in window coordinates. */
    private fun collect(view: View, into: MutableList<Rect>) {
        val location = IntArray(2).also(view::getLocationInWindow)
        view.systemGestureExclusionRects.forEach { rect ->
            into += Rect(rect).apply { offset(location[0], location[1]) }
        }
        if (view is ViewGroup) for (i in 0 until view.childCount) collect(view.getChildAt(i), into)
    }

    private companion object {
        const val TRIMMER = "trimmer"
    }
}
