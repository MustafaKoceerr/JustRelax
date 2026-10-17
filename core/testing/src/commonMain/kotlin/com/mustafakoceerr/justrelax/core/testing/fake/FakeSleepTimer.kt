package com.mustafakoceerr.justrelax.core.testing.fake

import com.mustafakoceerr.justrelax.core.playback.timer.SleepTimer
import com.mustafakoceerr.justrelax.core.playback.timer.TimerState
import com.mustafakoceerr.justrelax.core.playback.timer.TimerStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** State-only timer: no ticking. Tests call [expire] to make it run out. */
class FakeSleepTimer : SleepTimer {
    override val state = MutableStateFlow(TimerState())
    override val expirations = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    var cancelCount = 0
        private set

    override fun start(seconds: Long) {
        state.value = TimerState(TimerStatus.RUNNING, totalSeconds = seconds, remainingSeconds = seconds)
    }

    override fun pause() = state.update { it.copy(status = TimerStatus.PAUSED) }

    override fun resume() = state.update { it.copy(status = TimerStatus.RUNNING) }

    override fun cancel() {
        cancelCount++
        state.value = TimerState()
    }

    fun expire() {
        state.value = TimerState()
        expirations.tryEmit(Unit)
    }
}
