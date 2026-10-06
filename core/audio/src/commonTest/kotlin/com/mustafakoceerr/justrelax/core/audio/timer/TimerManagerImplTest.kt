package com.mustafakoceerr.justrelax.core.audio.timer

import com.mustafakoceerr.justrelax.core.domain.player.GlobalMixerState
import com.mustafakoceerr.justrelax.core.domain.player.SoundConfig
import com.mustafakoceerr.justrelax.core.domain.timer.TimerStatus
import com.mustafakoceerr.justrelax.core.testing.fake.FakeAudioMixer
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/** The sleep timer: counts down and stops all sounds when it reaches zero. */
class TimerManagerImplTest {

    private val mixer = FakeAudioMixer().apply {
        setState(GlobalMixerState(isPlaying = true, activeSounds = listOf(SoundConfig("rain", "/rain.mp3"))))
    }

    private fun TestScope.timer() = TimerManagerImpl(externalScope = backgroundScope, audioMixer = mixer)

    @Test
    fun countsDownEverySecond() = runTest {
        val timer = timer()

        timer.startTimer(seconds = 10)
        advanceTimeBy(3.seconds)
        runCurrent()

        assertEquals(TimerStatus.RUNNING, timer.state.value.status)
        assertEquals(7, timer.state.value.remainingSeconds)
    }

    @Test
    fun reachingZero_stopsAllSounds_andResets() = runTest {
        val timer = timer()

        timer.startTimer(seconds = 5)
        advanceTimeBy(6.seconds)
        runCurrent()

        assertTrue(mixer.state.value.activeSounds.isEmpty())
        assertEquals(TimerStatus.IDLE, timer.state.value.status)
    }

    @Test
    fun paused_doesNotCountDown() = runTest {
        val timer = timer()
        timer.startTimer(seconds = 10)
        advanceTimeBy(2.seconds)
        runCurrent()

        timer.pauseTimer()
        advanceTimeBy(5.seconds)
        runCurrent()

        assertEquals(TimerStatus.PAUSED, timer.state.value.status)
        assertEquals(8, timer.state.value.remainingSeconds)
    }

    @Test
    fun resume_continuesFromWhereItPaused() = runTest {
        val timer = timer()
        timer.startTimer(seconds = 10)
        advanceTimeBy(2.seconds)
        runCurrent()
        timer.pauseTimer()

        timer.resumeTimer()
        advanceTimeBy(3.seconds)
        runCurrent()

        assertEquals(5, timer.state.value.remainingSeconds)
    }

    @Test
    fun cancel_keepsSoundsPlaying() = runTest {
        val timer = timer()
        timer.startTimer(seconds = 5)

        timer.cancelTimer()
        advanceTimeBy(10.seconds)
        runCurrent()

        assertEquals(TimerStatus.IDLE, timer.state.value.status)
        assertTrue(mixer.state.value.isPlaying)
    }
}
