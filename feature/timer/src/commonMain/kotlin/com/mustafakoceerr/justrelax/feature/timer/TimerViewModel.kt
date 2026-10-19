package com.mustafakoceerr.justrelax.feature.timer

import com.mustafakoceerr.justrelax.core.analytics.logTimerStarted
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsHelper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.playback.timer.SleepTimer
import com.mustafakoceerr.justrelax.core.playback.timer.TimerStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class TimerViewModel(
    private val sleepTimer: SleepTimer,
    private val analytics: AnalyticsHelper,
    private val clock: Clock,
) : ViewModel() {

    val uiState: StateFlow<TimerUiState> = sleepTimer.state
        .map { timer ->
            TimerUiState(
                isSetupMode = timer.status == TimerStatus.IDLE,
                isPaused = timer.status == TimerStatus.PAUSED,
                totalSeconds = timer.totalSeconds,
                remainingSeconds = timer.remainingSeconds,
                endsAt = if (timer.status == TimerStatus.RUNNING) endTime(timer.remainingSeconds) else null,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimerUiState())

    /** Rounded down to the minute it is shown in, so it stays equal from one tick to the next. */
    private fun endTime(remainingSeconds: Long): Instant {
        val end = clock.now() + remainingSeconds.seconds
        return Instant.fromEpochSeconds(end.epochSeconds - end.epochSeconds.mod(60L))
    }

    fun startTimer(seconds: Long) {
        sleepTimer.start(seconds)
        analytics.logTimerStarted(seconds)
    }

    fun toggleTimer() {
        when (sleepTimer.state.value.status) {
            TimerStatus.RUNNING -> sleepTimer.pause()
            TimerStatus.PAUSED -> sleepTimer.resume()
            TimerStatus.IDLE -> Unit
        }
    }

    fun cancelTimer() = sleepTimer.cancel()
}
