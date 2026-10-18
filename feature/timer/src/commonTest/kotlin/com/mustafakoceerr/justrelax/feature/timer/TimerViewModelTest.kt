package com.mustafakoceerr.justrelax.feature.timer

import com.mustafakoceerr.justrelax.core.analytics.EventTypes
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsEvent
import com.mustafakoceerr.justrelax.core.testing.fake.TestAnalyticsHelper
import app.cash.turbine.test
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSleepTimer
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TimerViewModelTest {

    private val timer = FakeSleepTimer()
    private val analytics = TestAnalyticsHelper()
    private fun viewModel() = TimerViewModel(timer, analytics)

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
}
