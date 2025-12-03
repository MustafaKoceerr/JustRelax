package com.mustafakoceerr.justrelax.core.timer.domain.model

enum class TimerStatus {
    IDLE,    // Sayaç kurulu değil / bitti
    RUNNING, // Sayaç işliyor
    PAUSED   // Duraklatıldı
}