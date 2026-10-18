package com.mustafakoceerr.justrelax.feature.library

import kotlin.test.Test
import kotlin.test.assertEquals

class TimeOfDayTest {

    @Test
    fun theHour_picksTheGreeting() {
        assertEquals(TimeOfDay.MORNING, timeOfDayAt(hour = 5))
        assertEquals(TimeOfDay.MORNING, timeOfDayAt(hour = 11))
        assertEquals(TimeOfDay.AFTERNOON, timeOfDayAt(hour = 12))
        assertEquals(TimeOfDay.AFTERNOON, timeOfDayAt(hour = 16))
        assertEquals(TimeOfDay.EVENING, timeOfDayAt(hour = 17))
        assertEquals(TimeOfDay.EVENING, timeOfDayAt(hour = 21))
        assertEquals(TimeOfDay.NIGHT, timeOfDayAt(hour = 22))
        assertEquals(TimeOfDay.NIGHT, timeOfDayAt(hour = 0))
        assertEquals(TimeOfDay.NIGHT, timeOfDayAt(hour = 4))
    }
}
