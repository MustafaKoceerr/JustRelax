package com.mustafakoceerr.justrelax.feature.timer.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WheelMathTest {

    @Test
    fun theVirtualListIsBoundedAndHoldsWholeCycles() {
        val size = wheelItemCount(count = 60)
        assertTrue(size < Int.MAX_VALUE / 2)
        assertEquals(0, size % 60)
    }

    @Test
    fun theStartIndexIsInTheMiddleCycleAndShowsTheSelectedValue() {
        val index = startIndexFor(selected = 7, count = 60)
        assertEquals(7, valueAt(index, count = 60))
        assertTrue(index > wheelItemCount(60) / 3)
        assertTrue(index < wheelItemCount(60) * 2 / 3)
    }

    @Test
    fun valueAtWrapsAround() {
        assertEquals(0, valueAt(60, count = 60))
        assertEquals(59, valueAt(119, count = 60))
    }

    @Test
    fun nearestIndexTakesTheShorterWayRound() {
        val current = startIndexFor(selected = 58, count = 60)
        // 58 -> 2 is four steps forward, not 56 back.
        assertEquals(current + 4, nearestIndexFor(value = 2, currentIndex = current, count = 60))
        // 58 -> 50 is eight steps back.
        assertEquals(current - 8, nearestIndexFor(value = 50, currentIndex = current, count = 60))
        assertEquals(current, nearestIndexFor(value = 58, currentIndex = current, count = 60))
    }

    @Test
    fun theCenterItemIsFullSizeOpaqueAndFlat() {
        val center = wheelItemTransform(distance = 0f)
        assertEquals(1f, center.scale)
        assertEquals(1f, center.alpha)
        assertEquals(0f, center.rotationX)
    }

    @Test
    fun itemsAwayFromTheCenterShrinkFadeAndTiltAway() {
        val center = wheelItemTransform(0f)
        val above = wheelItemTransform(-1f)
        val below = wheelItemTransform(1f)

        assertTrue(below.scale < center.scale)
        assertTrue(below.alpha < center.alpha)
        assertEquals(above.scale, below.scale)
        assertEquals(above.alpha, below.alpha)
        // Mirror images: the top edge tilts back, the bottom edge forward.
        assertEquals(-above.rotationX, below.rotationX)
        assertTrue(below.rotationX != 0f)
    }

    @Test
    fun farItemsStopChanging() {
        assertEquals(wheelItemTransform(2f), wheelItemTransform(5f))
        assertTrue(wheelItemTransform(5f).alpha > 0f)
    }
}
