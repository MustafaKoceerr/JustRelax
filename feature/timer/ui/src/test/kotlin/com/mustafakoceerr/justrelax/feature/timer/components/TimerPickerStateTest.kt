package com.mustafakoceerr.justrelax.feature.timer.components

import kotlin.test.Test
import kotlin.test.assertEquals

class TimerPickerStateTest {

    @Test
    fun setSplitsTheTimeAcrossTheWheels() {
        val state = TimerPickerState()
        state.set(90 * 60L)

        assertEquals(1, state.hour)
        assertEquals(30, state.minute)
        assertEquals(0, state.second)
        assertEquals(5_400L, state.totalSeconds)
    }

    @Test
    fun setKeepsWithinWhatTheWheelsShow() {
        val state = TimerPickerState()
        state.set(30 * 60 * 60L)
        assertEquals(TimerPickerState.MAX_SECONDS, state.totalSeconds)

        state.set(-5)
        assertEquals(0L, state.totalSeconds)
    }

    @Test
    fun presetsAreSleepLengthsInAscendingOrder() {
        assertEquals(listOf(15, 30, 45, 60, 90, 120).map { it * 60L }, SLEEP_PRESETS_SECONDS)
    }
}
