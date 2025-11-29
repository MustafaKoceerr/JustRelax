package com.mustafakoceerr.justrelax.feature.timer

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.sound.domain.manager.SoundManager
import com.mustafakoceerr.justrelax.feature.player.PlayerViewModel
import com.mustafakoceerr.justrelax.feature.player.mvi.PlayerIntent
import com.mustafakoceerr.justrelax.feature.timer.domain.model.TimerStatus
import com.mustafakoceerr.justrelax.feature.timer.mvi.TimerEffect
import com.mustafakoceerr.justrelax.feature.timer.mvi.TimerIntent
import com.mustafakoceerr.justrelax.feature.timer.mvi.TimerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Senkronizasyon Sorunu Çözüldü:
 * Timer durdurunca Player'ın haberi olmama riski ortadan kalktı.
 * Çünkü ikisi de aynı SoundManager state'ine bakıyor.
 *
 * Anti-Pattern Gitti: ViewModel içinde ViewModel tutma ayıbından kurtulduk.
 * Test Edilebilirlik: SoundManager'ı UI olmadan, sadece Unit Test ile test edebilirsin.
 *
 */
class TimerViewModel(
    // bu bir antipattern'dir.
    private val soundManager: SoundManager // DEĞİŞİKLİK: PlayerViewModel -> SoundManager
) : ScreenModel {
    // State: UI'ın dinlediği veri kaynağı
    private val _state = MutableStateFlow(TimerState())
    val state = _state.asStateFlow()

    // effect: Tek seferlik olaylar (Örn: süre bitti toast mesajı)
    private val _effect = Channel<TimerEffect>()
    val effect = _effect.receiveAsFlow()

    // Geri sayım işlemini tutan Coroutine Job
    private var timerJob: Job? = null

    fun processIntent(intent: TimerIntent) {
        when (intent) {
            is TimerIntent.StartTimer -> startTimer(intent.totalSeconds)
            TimerIntent.PauseTimer -> pauseTimer()
            TimerIntent.ResumeTimer -> resumeTimer()
            TimerIntent.CancelTimer -> cancelTimer()
        }
    }

    private fun startTimer(totalSeconds: Long) {
        // Var olan bir sayaç varsa önce onu temizle
        timerJob?.cancel()

        // 1. State'i güncelle: Running moduna geç
        _state.update {
            it.copy(
                status = TimerStatus.RUNNING,
                totalTimeSeconds = totalSeconds,
                timeLeftSeconds = totalSeconds
            )
        }

        // 2. Geri sayımı başlat
        startCountdown()
    }

    private fun pauseTimer() {
        // Job'ı iptal et (Geri sayım durur)
        timerJob?.cancel()

        // State'i güncelle: Paused moduna geç (süre sıfırlanmaz)
        _state.update { it.copy(status = TimerStatus.PAUSED) }
    }

    private fun resumeTimer() {
        // Kaldığı yerden devam etmek için tekrar RUNNING yap
        _state.update { it.copy(status = TimerStatus.RUNNING) }

        // Geri sayımı tekrar başlat (Mevcut timeLeft üzerinden devam eder)
        startCountdown()
    }

    private fun cancelTimer() {
        // Her şeyi durdur ve sıfırla
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
        // coroutine başlatıyoruz.
        timerJob = screenModelScope.launch {
            while (_state.value.timeLeftSeconds > 0) {
                delay(1000L) // 1 saniye bekle
                // state'i güncelle: Süreyi 1 azalt
                _state.update {
                    it.copy(timeLeftSeconds = it.timeLeftSeconds - 1)
                }
            }

            // Döngü bittiyse süre bitmiştir
            onTimerFinished()
        }
    }

    private fun onTimerFinished() {
        // 1. Timer'ı sıfırla (Idle moduna dön)
        cancelTimer()

        // 2. PlayerViewModel'e "Stop All" emri gönder.
        soundManager.stopAll()

        // UI'a haber ver (opsiyonel)
        screenModelScope.launch {
            _effect.send(TimerEffect.TimerFinished)
        }
    }

    override fun onDispose() {
        // ViewModel ölürse sayacı durdur (Memory Leak önlemi)
        timerJob?.cancel()
        super.onDispose()
    }

}