package com.mustafakoceerr.justrelax.feature.timer.mvi

interface TimerContract {
    data class State(
        val isSetupMode: Boolean = true,
        val totalSeconds: Long = 0L,
        val remainingSeconds: Long = 0L,
        val isPaused: Boolean = false
    )

    sealed interface Event {
        data class StartTimer(val durationSeconds: Long) : Event
        data object ToggleTimer : Event
        data object CancelTimer : Event
    }

    sealed interface Effect {
    }
}