package com.mustafakoceerr.justrelax.core.domain.timer

data class TimerState(
    val status: TimerStatus = TimerStatus.IDLE,
    val totalSeconds: Long = 0L,     // Kurulan toplam süre (Örn: 1800 sn)
    val remainingSeconds: Long = 0L  // Kalan süre (Örn: 1200 sn)
)