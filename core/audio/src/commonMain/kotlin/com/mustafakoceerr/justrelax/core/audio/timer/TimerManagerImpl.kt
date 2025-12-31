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

/**
 * TimerManager implementasyonu.
 * Bu sınıf Singleton olarak (Koin ile) oluşturulmalı ve Application Scope verilmelidir.
 */
class TimerManagerImpl(
    private val externalScope: CoroutineScope, // Uygulama/Servis ölçeğinde scope
    private val stopAllSoundsUseCase: StopAllSoundsUseCase
) : TimerManager {
    private val _state = MutableStateFlow(TimerState())
    override val state = _state.asStateFlow()

    private var timerJob: Job? = null

    override fun startTimer(seconds: Long) {
        // Güvenlik: Önceki sayacı temizle
        timerJob?.cancel()

        // State'i güncelle
        _state.update {
            it.copy(
                status = TimerStatus.RUNNING,
                totalSeconds = seconds,
                remainingSeconds = seconds
            )
        }

        // Geri sayımı başlat
        startTicker()
    }

    override fun pauseTimer() {
        timerJob?.cancel()
        _state.update { it.copy(status = TimerStatus.PAUSED) }
    }

    override fun resumeTimer() {
        // Sadece kalan süre varsa devam et
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

    /**
     * Saniye saniye sayan döngü.
     */
    private fun startTicker() {
        timerJob = externalScope.launch {
            while (_state.value.remainingSeconds > 0) {
                delay(1000L) // 1 saniye bekle

                _state.update { current ->
                    val newRemaining = current.remainingSeconds - 1
                    current.copy(remainingSeconds = newRemaining)
                }
            }

            // Döngü bitti (Süre 0 oldu) -> Timer Tamamlandı
            onTimerFinished()
        }
    }

    private fun onTimerFinished() {
        // 1. Sesleri durdur (Servis de duracaktır)
        externalScope.launch {
            stopAllSoundsUseCase()
        }

        // 2. Timer state'ini sıfırla
        _state.update { TimerState(status = TimerStatus.IDLE) }
    }
}