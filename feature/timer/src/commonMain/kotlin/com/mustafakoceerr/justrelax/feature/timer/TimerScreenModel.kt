package com.mustafakoceerr.justrelax.feature.timer

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.domain.timer.TimerManager
import com.mustafakoceerr.justrelax.core.domain.timer.TimerStatus
import com.mustafakoceerr.justrelax.feature.timer.mvi.TimerEffect
import com.mustafakoceerr.justrelax.feature.timer.mvi.TimerIntent
import com.mustafakoceerr.justrelax.feature.timer.mvi.TimerState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TimerScreenModel(
    private val timerManager: TimerManager
) : ScreenModel {

    val state: StateFlow<TimerState> = timerManager.state
        .map { domainState ->
            // Domain'den geleni direkt UI'a yansıtıyoruz.
            TimerState(
                status = domainState.status,
                totalSeconds = domainState.totalSeconds,
                remainingSeconds = domainState.remainingSeconds
            )
        }
        .onEach { newState ->
            // Süre dolduğunda Effect gönder
            if (currentState.status == TimerStatus.RUNNING && newState.status == TimerStatus.IDLE) {
                sendEffect(TimerEffect.TimerFinished)
            }
        }
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TimerState()
        )

    private val _effect = Channel<TimerEffect>()
    val effect = _effect.receiveAsFlow()

    private val currentState: TimerState get() = state.value

    fun onIntent(intent: TimerIntent) {
        when (intent) {
            is TimerIntent.Start -> {
                // Picker'dan gelen saniyeyi al ve başlat
                timerManager.startTimer(intent.totalSeconds)
            }
            TimerIntent.Toggle -> {
                // Mevcut duruma göre duraklat veya devam et
                if (currentState.status == TimerStatus.RUNNING) {
                    timerManager.pauseTimer()
                } else if (currentState.status == TimerStatus.PAUSED) {
                    timerManager.resumeTimer()
                }
            }
            TimerIntent.Cancel -> {
                timerManager.cancelTimer()
            }
        }
    }

    private fun sendEffect(effect: TimerEffect) {
        screenModelScope.launch {
            _effect.send(effect)
        }
    }
}