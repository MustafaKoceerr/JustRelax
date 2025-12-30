package com.mustafakoceerr.justrelax.core.domain.timer

import kotlinx.coroutines.flow.StateFlow

/**
 * Uygulamanın zamanlayıcısını yöneten sözleşme (Contract).
 * Bu arayüz Domain katmanındadır, implementation'ı Data/Audio katmanında olacaktır.
 */
// 3. CONTRACT (INTERFACE)
interface TimerManager {
    /**
     * UI'ın dinleyeceği tek gerçeklik kaynağı.
     * Flow yerine StateFlow kullanıyoruz ki UI açıldığında son durumu (örn: 12:45 kaldı) anında görsün.
     */
    val state: StateFlow<TimerState>

    /**
     * Sayacı başlatır.
     * @param seconds: Kaç saniye sayılacak?
     */
    fun startTimer(seconds: Long)

    /**
     * Sayacı duraklatır.
     */
    fun pauseTimer()

    /**
     * Duraklatılan sayacı devam ettirir.
     */
    fun resumeTimer()

    /**
     * Sayacı tamamen iptal eder ve sıfırlar.
     */
    fun cancelTimer()
}