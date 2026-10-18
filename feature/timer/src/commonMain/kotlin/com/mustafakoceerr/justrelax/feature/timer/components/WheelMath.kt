package com.mustafakoceerr.justrelax.feature.timer.components

import kotlin.math.abs

/** Full turns in the wheel's list: enough to feel endless, small enough for accessibility services. */
private const val VIRTUAL_CYCLES = 1_000

/** Items beyond this many rows from the center look the same as at this distance. */
private const val MAX_DISTANCE = 2f

internal fun wheelItemCount(count: Int): Int = count * VIRTUAL_CYCLES

/** The list index that shows [selected] in the middle cycle, so the wheel can turn either way. */
internal fun startIndexFor(selected: Int, count: Int): Int = VIRTUAL_CYCLES / 2 * count + selected

internal fun valueAt(index: Int, count: Int): Int = index.mod(count)

/** The index near [currentIndex] that shows [value], reached the shorter way round. */
internal fun nearestIndexFor(value: Int, currentIndex: Int, count: Int): Int {
    var steps = (value - valueAt(currentIndex, count)).mod(count)
    if (steps > count / 2) steps -= count
    return currentIndex + steps
}

internal data class WheelItemTransform(val scale: Float, val alpha: Float, val rotationX: Float)

/**
 * How an item looks [distance] rows from the center (negative is above): it shrinks, fades and
 * tilts away, as on a drum.
 */
internal fun wheelItemTransform(distance: Float): WheelItemTransform {
    val signed = distance.coerceIn(-MAX_DISTANCE, MAX_DISTANCE)
    val away = abs(signed)
    return WheelItemTransform(
        scale = 1f - 0.15f * away,
        alpha = 1f - 0.45f * away,
        rotationX = 25f * (0f - signed),
    )
}

/** The share of the ring still filled. */
internal fun ringProgress(remainingSeconds: Long, totalSeconds: Long): Float =
    if (totalSeconds <= 0) 0f else (remainingSeconds.toFloat() / totalSeconds).coerceIn(0f, 1f)

/**
 * Decides which resting positions of a wheel are the user's choice. A turn the app starts (a chip,
 * typing) is not reported back, nor is the spot where a newer turn cut it short.
 */
internal class WheelRestReporter {
    private var appTarget: Int? = null

    fun onTurnTo(value: Int) {
        appTarget = value
    }

    fun onUserDrag() {
        appTarget = null
    }

    /** The value to report for a wheel at rest on [value], or null. */
    fun onRest(value: Int): Int? {
        val target = appTarget ?: return value
        if (value == target) appTarget = null
        return null
    }
}
