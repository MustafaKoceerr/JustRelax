package com.mustafakoceerr.justrelax.feature.timer

import com.mustafakoceerr.justrelax.core.analytics.EventTypes
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsEvent
import com.mustafakoceerr.justrelax.core.testing.fake.TestAnalyticsHelper
import app.cash.turbine.test
import com.mustafakoceerr.justrelax.core.playback.timer.TimerState
import com.mustafakoceerr.justrelax.core.playback.timer.TimerStatus
import com.mustafakoceerr.justrelax.core.testing.fake.FakeClock
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSleepTimer
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class TimerViewModelTest {

    private val timer = FakeSleepTimer()
    private val analytics = TestAnalyticsHelper()
    // 10:00:30 UTC
    private val clock = FakeClock(Instant.parse("2026-10-18T10:00:30Z"))
    private fun viewModel() = TimerViewModel(timer, analytics, clock)

    @Test
    fun idleTimer_showsSetup() = runMainTest {
        viewModel().uiState.test {
            assertTrue(awaitItem().isSetupMode)
        }
    }

    @Test
    fun startingTimer_showsCountdown() = runMainTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitItem()
            viewModel.startTimer(seconds = 600)

            val state = awaitItem()
            assertFalse(state.isSetupMode)
            assertEquals(600, state.totalSeconds)
            assertEquals(600, state.remainingSeconds)
        }
    }

    @Test
    fun toggle_pausesAndResumes() = runMainTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitItem()
            viewModel.startTimer(seconds = 60)
            awaitItem()

            viewModel.toggleTimer()
            assertTrue(awaitItem().isPaused)

            viewModel.toggleTimer()
            assertFalse(awaitItem().isPaused)
        }
    }

    @Test
    fun cancel_returnsToSetup() = runMainTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitItem()
            viewModel.startTimer(seconds = 60)
            awaitItem()

            viewModel.cancelTimer()
            assertTrue(awaitItem().isSetupMode)
        }
    }

    @Test
    fun aStartedTimerIsCountedByRoughLength() = runMainTest {
        val viewModel = viewModel()

        viewModel.startTimer(10 * 60)
        viewModel.startTimer(45 * 60)
        viewModel.startTimer(3 * 60 * 60)

        assertEquals(
            listOf("under_15m", "30_60m", "over_2h"),
            analytics.events.filter { it.type == EventTypes.TIMER_STARTED }.map { it.params.getValue("duration_bucket") },
        )
    }

    @Test
    fun aRunningTimerShowsWhenItEnds_toTheMinute() = runMainTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitItem()
            viewModel.startTimer(seconds = 600)

            // 10:00:30 + 10 min = 10:10:30, shown as 10:10.
            assertEquals(Instant.parse("2026-10-18T10:10:00Z"), awaitItem().endsAt)
        }
    }

    @Test
    fun theEndTimeStaysTheSameWhileTheTimerTicks() = runMainTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitItem()
            viewModel.startTimer(seconds = 600)
            val first = awaitItem().endsAt

            clock.current += 1.seconds
            timer.state.value = TimerState(TimerStatus.RUNNING, totalSeconds = 600, remainingSeconds = 599)

            val ticked = awaitItem()
            assertEquals(599, ticked.remainingSeconds)
            assertEquals(first, ticked.endsAt)
        }
    }

    @Test
    fun aPausedOrIdleTimerHasNoEndTime() = runMainTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            assertNull(awaitItem().endsAt)
            viewModel.startTimer(seconds = 600)
            awaitItem()

            viewModel.toggleTimer()
            assertNull(awaitItem().endsAt)
        }
    }
}
