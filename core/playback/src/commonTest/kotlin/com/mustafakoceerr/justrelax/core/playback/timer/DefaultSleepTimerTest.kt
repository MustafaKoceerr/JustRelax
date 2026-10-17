package com.mustafakoceerr.justrelax.core.playback.timer

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.testTimeSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class DefaultSleepTimerTest {

    private var expirations = 0

    private fun TestScope.timer() = DefaultSleepTimer(backgroundScope, testTimeSource).also { timer ->
        backgroundScope.launch { timer.expirations.collect { expirations++ } }
        runCurrent()
    }

    private fun TestScope.advance(seconds: Int) {
        advanceTimeBy(seconds.seconds)
        runCurrent()
    }

    @Test
    fun countsDownEverySecond() = runTest {
        val timer = timer()

        timer.start(seconds = 10)
        advance(3)

        assertEquals(TimerStatus.RUNNING, timer.state.value.status)
        assertEquals(7, timer.state.value.remainingSeconds)
        assertEquals(10, timer.state.value.totalSeconds)
    }

    @Test
    fun reachingZero_expiresOnce_andResets() = runTest {
        val timer = timer()

        timer.start(seconds = 5)
        advance(6)

        assertEquals(1, expirations)
        assertEquals(TimerStatus.IDLE, timer.state.value.status)
    }

    @Test
    fun paused_doesNotCountDown() = runTest {
        val timer = timer()
        timer.start(seconds = 10)
        advance(2)

        timer.pause()
        advance(5)

        assertEquals(TimerStatus.PAUSED, timer.state.value.status)
        assertEquals(8, timer.state.value.remainingSeconds)
        assertEquals(0, expirations)
    }

    @Test
    fun resume_continuesFromWhereItPaused() = runTest {
        val timer = timer()
        timer.start(seconds = 10)
        advance(2)
        timer.pause()

        timer.resume()
        advance(3)

        assertEquals(5, timer.state.value.remainingSeconds)
    }

    @Test
    fun pausingMidSecond_keepsTheExactRemainingTime() = runTest {
        val timer = timer()
        timer.start(seconds = 3)
        advanceTimeBy(2500.milliseconds)
        timer.pause()
        advance(10)

        timer.resume()
        advanceTimeBy(400.milliseconds)
        runCurrent()
        assertEquals(0, expirations)

        advanceTimeBy(200.milliseconds)
        runCurrent()
        assertEquals(1, expirations)
    }

    @Test
    fun cancel_neverExpires() = runTest {
        val timer = timer()
        timer.start(seconds = 5)

        timer.cancel()
        advance(10)

        assertEquals(TimerStatus.IDLE, timer.state.value.status)
        assertEquals(0, expirations)
    }
}
