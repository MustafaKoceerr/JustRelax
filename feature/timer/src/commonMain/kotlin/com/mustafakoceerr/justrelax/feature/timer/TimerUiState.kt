package com.mustafakoceerr.justrelax.feature.timer

data class TimerUiState(
    val isSetupMode: Boolean = true,
    val totalSeconds: Long = 0L,
    val remainingSeconds: Long = 0L,
    val isPaused: Boolean = false,
)
