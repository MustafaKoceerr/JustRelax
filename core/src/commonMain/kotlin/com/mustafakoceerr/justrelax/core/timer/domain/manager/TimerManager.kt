package com.mustafakoceerr.justrelax.core.timer.domain.manager

import com.mustafakoceerr.justrelax.core.sound.domain.manager.SoundController
import com.mustafakoceerr.justrelax.core.timer.domain.model.TimerStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TimerManagerState(
    val status: TimerStatus = TimerStatus.IDLE,
    val totalTimeSeconds: Long = 0L,
    val timeLeftSeconds: Long = 0L
)

class TimerManager(
    private val soundController: SoundController // <--- DEĞİŞİKLİK BURADA
) {
    // Singleton scope( uygulama yaşadığı sürece yaşar.)
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _state = MutableStateFlow(TimerManagerState())
    val state = _state.asStateFlow()

    private var timerJob: Job
    ? = null

    fun startTimer(totalSeconds: Long) {
        timerJob?.cancel()
        _state.update {
            it.copy(
                status = TimerStatus.RUNNING,
                totalTimeSeconds = totalSeconds,
                timeLeftSeconds = totalSeconds
            )
        }
        startCountdown()
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _state.update { it.copy(status = TimerStatus.PAUSED) }
    }

    fun resumeTimer() {
        _state.update { it.copy(status = TimerStatus.RUNNING) }
        startCountdown()
    }

    fun cancelTimer() {
        timerJob?.cancel()
        _state.update {
            it.copy(
                status = TimerStatus.IDLE,
                timeLeftSeconds = 0,
                totalTimeSeconds = 0
            )
        }
    }

    private fun startCountdown() {
        timerJob = scope.launch {
            while (_state.value.timeLeftSeconds > 0) {
                delay(1000L)
                _state.update {
                    it.copy(timeLeftSeconds = it.timeLeftSeconds - 1)
                }
            }
            // Süre Bitti!
            onTimerFinished()
        }
    }

    private fun onTimerFinished() {
        // 1. Sesleri Durdur
        soundController.stopAll()

        // 2. Timer'ı sıfırla (Idle moda çek)
        cancelTimer()
    }
}

