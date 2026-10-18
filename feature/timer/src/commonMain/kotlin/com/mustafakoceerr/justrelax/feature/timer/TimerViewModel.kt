package com.mustafakoceerr.justrelax.feature.timer

import com.mustafakoceerr.justrelax.core.analytics.Params
import com.mustafakoceerr.justrelax.core.analytics.EventTypes
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsEvent
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsHelper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.playback.timer.SleepTimer
import com.mustafakoceerr.justrelax.core.playback.timer.TimerStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TimerViewModel(
    private val sleepTimer: SleepTimer,
    private val analytics: AnalyticsHelper,
) : ViewModel() {

    val uiState: StateFlow<TimerUiState> = sleepTimer.state
        .map { timer ->
            TimerUiState(
                isSetupMode = timer.status == TimerStatus.IDLE,
                isPaused = timer.status == TimerStatus.PAUSED,
                totalSeconds = timer.totalSeconds,
                remainingSeconds = timer.remainingSeconds,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimerUiState())

    fun startTimer(seconds: Long) {
        sleepTimer.start(seconds)
        analytics.logEvent(AnalyticsEvent(EventTypes.TIMER_STARTED, mapOf(Params.DURATION_BUCKET to durationBucket(seconds))))
    }

    /** A rough length, enough to see how people use the timer. */
    private fun durationBucket(seconds: Long): String = when {
        seconds < 15 * 60 -> "under_15m"
        seconds < 30 * 60 -> "15_30m"
        seconds <= 60 * 60 -> "30_60m"
        seconds <= 2 * 60 * 60 -> "1_2h"
        else -> "over_2h"
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
