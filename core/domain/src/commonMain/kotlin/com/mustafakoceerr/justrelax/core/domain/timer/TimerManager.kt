package com.mustafakoceerr.justrelax.core.domain.timer

import kotlinx.coroutines.flow.Flow

/**
 * Uygulamanın zamanlayıcısını yöneten sözleşme (Contract).
 * Bu arayüz Domain katmanındadır, implementation'ı Data/Audio katmanında olacaktır.
 */
interface TimerManager {
    // UI'ın dinleyeceği anlık durum (Saniye saniye güncellenir)
    val state: Flow<TimerState>

    // Timer şu an aktif mi? (Running veya Paused)
    val isRunning: Flow<Boolean>

    /**
     * Sayacı başlatır.
     * @param seconds: Kaç saniye sayılacak?
     */
    fun startTimer(seconds: Long)

    /**
     * Sayacı duraklatır (Süre akmaz).
     */
    fun pauseTimer()

    /**
     * Duraklatılan sayacı devam ettirir.
     */
    fun resumeTimer()

    /**
     * Sayacı tamamen iptal eder ve sıfırlar (IDLE).
     */
    fun cancelTimer()
}