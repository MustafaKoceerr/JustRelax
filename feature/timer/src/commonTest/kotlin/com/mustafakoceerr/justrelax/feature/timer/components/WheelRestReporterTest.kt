package com.mustafakoceerr.justrelax.feature.timer.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WheelRestReporterTest {

    private val reporter = WheelRestReporter()

    @Test
    fun aWheelTheUserTurnedReportsWhereItStops() {
        assertEquals(12, reporter.onRest(12))
    }

    @Test
    fun aTurnStartedByTheAppIsNotReportedBack() {
        reporter.onTurnTo(45)
        assertNull(reporter.onRest(45))
        // Later turns by the user are reported again.
        assertEquals(46, reporter.onRest(46))
    }

    @Test
    fun anInterruptedTurnDoesNotReportWhereItWasCut() {
        // Typing "4" then "45": the turn to 4 is cut short by the turn to 45.
        reporter.onTurnTo(4)
        reporter.onTurnTo(45)
        assertNull(reporter.onRest(57))
        assertNull(reporter.onRest(45))
    }

    @Test
    fun theUserGrabbingTheWheelCancelsTheAppsTurn() {
        reporter.onTurnTo(45)
        reporter.onUserDrag()
        assertEquals(30, reporter.onRest(30))
    }
}
