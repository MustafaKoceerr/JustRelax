package com.mustafakoceerr.justrelax.feature.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.domain.timer.TimerManager
import com.mustafakoceerr.justrelax.core.domain.timer.TimerStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TimerViewModel(
    private val timerManager: TimerManager,
) : ViewModel() {

    val uiState: StateFlow<TimerUiState> = timerManager.state
        .map { timer ->
            TimerUiState(
                isSetupMode = timer.status == TimerStatus.IDLE,
                isPaused = timer.status == TimerStatus.PAUSED,
                totalSeconds = timer.totalSeconds,
                remainingSeconds = timer.remainingSeconds,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimerUiState())

    fun startTimer(seconds: Long) = timerManager.startTimer(seconds)

    fun toggleTimer() {
        when (timerManager.state.value.status) {
            TimerStatus.RUNNING -> timerManager.pauseTimer()
            TimerStatus.PAUSED -> timerManager.resumeTimer()
            TimerStatus.IDLE -> Unit
        }
    }

    fun cancelTimer() = timerManager.cancelTimer()
}
