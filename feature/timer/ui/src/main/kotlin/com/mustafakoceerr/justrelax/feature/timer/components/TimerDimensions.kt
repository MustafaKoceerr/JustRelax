package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mustafakoceerr.justrelax.core.ui.util.WindowWidthSize

/** Sizes on the timer screens, picked once for the window. */
data class TimerDimensions(
    val wheelItemHeight: Dp,
    val wheelWidth: Dp,
    val wheelDigitSize: TextUnit,
    val unitLabelSize: TextUnit,
    val countdownSize: TextUnit,
    val captionSize: TextUnit,
    val ringStroke: Dp,
) {
    companion object {
        /**
         * A short window (a phone held sideways) gets small wheels whatever its width: a landscape
         * phone is often a medium-width window, and those wheels do not fit its height.
         */
        fun forWindow(width: WindowWidthSize, isCompactHeight: Boolean): TimerDimensions =
            if (isCompactHeight) SHORT else forWidth(width)

        private val SHORT = TimerDimensions(56.dp, 72.dp, 36.sp, 13.sp, 44.sp, 14.sp, 8.dp)

        private fun forWidth(width: WindowWidthSize): TimerDimensions = when (width) {
            WindowWidthSize.COMPACT -> TimerDimensions(72.dp, 84.dp, 44.sp, 14.sp, 56.sp, 16.sp, 10.dp)
            WindowWidthSize.MEDIUM -> TimerDimensions(88.dp, 104.dp, 56.sp, 16.sp, 72.sp, 20.sp, 12.dp)
            WindowWidthSize.EXPANDED -> TimerDimensions(100.dp, 120.dp, 64.sp, 18.sp, 84.sp, 22.sp, 14.dp)
        }
    }
}
