package com.mustafakoceerr.justrelax.feature.timer

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.domain.timer.TimerManager
import com.mustafakoceerr.justrelax.core.domain.timer.TimerStatus
import com.mustafakoceerr.justrelax.feature.timer.mvi.TimerContract
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class TimerViewModel(
    private val timerManager: TimerManager
) : ScreenModel {

    private val _state = MutableStateFlow(TimerContract.State())
    val state = _state.asStateFlow()

    private val _effect = Channel<TimerContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        observeTimerState()
    }

    fun onEvent(event: TimerContract.Event) {
        when (event) {
            is TimerContract.Event.StartTimer -> timerManager.startTimer(event.durationSeconds)
            TimerContract.Event.CancelTimer -> timerManager.cancelTimer()
            TimerContract.Event.ToggleTimer -> handleToggleTimer()
        }
    }

    private fun handleToggleTimer() {
        when (timerManager.state.value.status) {
            TimerStatus.RUNNING -> timerManager.pauseTimer()
            TimerStatus.PAUSED -> timerManager.resumeTimer()
            TimerStatus.IDLE -> { /* No-op */ }
        }
    }

    private fun observeTimerState() {
        timerManager.state.onEach { domainState ->
            _state.update {
                it.copy(
                    isSetupMode = domainState.status == TimerStatus.IDLE,
                    isPaused = domainState.status == TimerStatus.PAUSED,
                    totalSeconds = domainState.totalSeconds,
                    remainingSeconds = domainState.remainingSeconds
                )
            }
        }.launchIn(screenModelScope)
    }
}