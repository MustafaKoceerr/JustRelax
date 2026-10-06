package com.mustafakoceerr.justrelax.core.testing.fake

import com.mustafakoceerr.justrelax.core.domain.timer.TimerManager
import com.mustafakoceerr.justrelax.core.domain.timer.TimerState
import com.mustafakoceerr.justrelax.core.domain.timer.TimerStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** State-only timer: no ticking, tests drive [state] directly when needed. */
class FakeTimerManager : TimerManager {
    override val state = MutableStateFlow(TimerState())

    override fun startTimer(seconds: Long) {
        state.value = TimerState(TimerStatus.RUNNING, totalSeconds = seconds, remainingSeconds = seconds)
    }

    override fun pauseTimer() = state.update { it.copy(status = TimerStatus.PAUSED) }

    override fun resumeTimer() = state.update { it.copy(status = TimerStatus.RUNNING) }

    override fun cancelTimer() {
        state.value = TimerState()
    }
}
