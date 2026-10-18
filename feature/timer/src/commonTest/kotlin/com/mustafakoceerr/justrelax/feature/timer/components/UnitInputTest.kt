package com.mustafakoceerr.justrelax.feature.timer.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UnitInputTest {

    @Test
    fun keepsAtMostTwoDigits() {
        assertEquals("12", sanitizeUnitInput("123"))
        assertEquals("05", sanitizeUnitInput("0a5"))
        assertEquals("", sanitizeUnitInput("-,."))
    }

    @Test
    fun parsesAndCapsAtTheUnitsMaximum() {
        assertEquals(7, parseUnitInput("07", max = 59))
        assertEquals(59, parseUnitInput("75", max = 59))
        assertEquals(23, parseUnitInput("30", max = 23))
    }

    @Test
    fun emptyInputKeepsTheCurrentValue() {
        assertNull(parseUnitInput("", max = 59))
    }
}
