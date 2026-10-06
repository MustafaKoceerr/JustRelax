package com.mustafakoceerr.justrelax.feature.timer

import app.cash.turbine.test
import com.mustafakoceerr.justrelax.core.testing.fake.FakeTimerManager
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TimerViewModelTest {

    private val timer = FakeTimerManager()
    private fun viewModel() = TimerViewModel(timer)

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
}
