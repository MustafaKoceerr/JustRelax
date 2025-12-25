package com.mustafakoceerr.justrelax.feature.timer.mvi

import com.mustafakoceerr.justrelax.core.domain.timer.TimerStatus

// 1. STATE: UI'ın ihtiyacı olan her şey
data class TimerState(
    // Timer'ın genel durumu (IDLE, RUNNING, PAUSED)
    // Bu durum, TimerSetupScreen mi yoksa TimerPortraitLayout mu gösterilecek, onu belirler.
    val status: TimerStatus = TimerStatus.IDLE,

    // Timer çalışırken kullanılan değerler
    val totalSeconds: Long = 0L,
    val remainingSeconds: Long = 0L
)

// 2. INTENT: Kullanıcı eylemleri
sealed interface TimerIntent {
    // TimerSetupScreen'deki "Başlat" butonuna tıklandı
    data class Start(val totalSeconds: Long) : TimerIntent

    // TimerButtonsRow'daki Play/Pause butonuna tıklandı
    data object Toggle : TimerIntent

    // TimerButtonsRow'daki Cancel butonuna tıklandı
    data object Cancel : TimerIntent
}

// 3. EFFECT: Tek seferlik olaylar
sealed interface TimerEffect {
    // Süre dolduğunda bir mesaj göstermek için
    data object TimerFinished : TimerEffect
}