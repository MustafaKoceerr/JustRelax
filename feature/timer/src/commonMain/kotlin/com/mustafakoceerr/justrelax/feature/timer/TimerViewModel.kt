package com.mustafakoceerr.justrelax.feature.timer

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

    fun startTimer(seconds: Long) = sleepTimer.start(seconds)

    fun toggleTimer() {
        when (sleepTimer.state.value.status) {
            TimerStatus.RUNNING -> sleepTimer.pause()
            TimerStatus.PAUSED -> sleepTimer.resume()
            TimerStatus.IDLE -> Unit
        }
    }

    fun cancelTimer() = sleepTimer.cancel()
}
