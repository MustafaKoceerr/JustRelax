package com.mustafakoceerr.justrelax.feature.timer

// IMPORTLAR GÜNCELLENDİ
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.timer.domain.manager.TimerManager
import com.mustafakoceerr.justrelax.feature.timer.mvi.TimerIntent
import com.mustafakoceerr.justrelax.feature.timer.mvi.TimerState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TimerViewModel(
    private val timerManager: TimerManager // Core'daki Manager
) : ScreenModel {

    val state: StateFlow<TimerState> = timerManager.state
        .map { managerState ->
            TimerState(
                status = managerState.status,
                totalTimeSeconds = managerState.totalTimeSeconds,
                timeLeftSeconds = managerState.timeLeftSeconds
            )
        }
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TimerState()
        )



    fun processIntent(intent: TimerIntent) {
        when (intent) {
            is TimerIntent.StartTimer -> timerManager.startTimer(intent.totalSeconds)
            TimerIntent.PauseTimer -> timerManager.pauseTimer()
            TimerIntent.ResumeTimer -> timerManager.resumeTimer()
            TimerIntent.CancelTimer -> timerManager.cancelTimer()
        }
    }
}