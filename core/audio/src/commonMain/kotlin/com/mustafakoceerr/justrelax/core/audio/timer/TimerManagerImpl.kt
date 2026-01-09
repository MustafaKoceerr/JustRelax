package com.mustafakoceerr.justrelax.core.audio.timer

import com.mustafakoceerr.justrelax.core.domain.timer.TimerManager
import com.mustafakoceerr.justrelax.core.domain.timer.TimerState
import com.mustafakoceerr.justrelax.core.domain.timer.TimerStatus
import com.mustafakoceerr.justrelax.core.domain.usecase.player.StopAllSoundsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TimerManagerImpl(
    private val externalScope: CoroutineScope,
    private val stopAllSoundsUseCase: StopAllSoundsUseCase
) : TimerManager {
    private val _state = MutableStateFlow(TimerState())
    override val state = _state.asStateFlow()

    private var timerJob: Job? = null

    override fun startTimer(seconds: Long) {
        timerJob?.cancel()

        _state.update {
            it.copy(
                status = TimerStatus.RUNNING,
                totalSeconds = seconds,
                remainingSeconds = seconds
            )
        }

        startTicker()
    }

    override fun pauseTimer() {
        timerJob?.cancel()
        _state.update { it.copy(status = TimerStatus.PAUSED) }
    }

    override fun resumeTimer() {
        if (_state.value.remainingSeconds > 0) {
            _state.update { it.copy(status = TimerStatus.RUNNING) }
            startTicker()
        } else {
            cancelTimer()
        }
    }

    override fun cancelTimer() {
        timerJob?.cancel()
        _state.update { TimerState(status = TimerStatus.IDLE) }
    }

    private fun startTicker() {
        timerJob = externalScope.launch {
            while (_state.value.remainingSeconds > 0) {
                delay(1000L)
                _state.update { current ->
                    val newRemaining = current.remainingSeconds - 1
                    current.copy(remainingSeconds = newRemaining)
                }
            }
            onTimerFinished()
        }
    }

    private fun onTimerFinished() {
        externalScope.launch {
            stopAllSoundsUseCase()
        }
        _state.update { TimerState(status = TimerStatus.IDLE) }
    }
}