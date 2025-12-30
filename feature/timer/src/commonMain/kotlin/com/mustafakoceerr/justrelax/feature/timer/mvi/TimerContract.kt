package com.mustafakoceerr.justrelax.feature.timer.mvi

import com.mustafakoceerr.justrelax.core.domain.timer.TimerStatus
import com.mustafakoceerr.justrelax.core.ui.util.UiText

/**
 * Timer ekranı için MVI sözleşmesi.
 * UI'ın ihtiyacı olan tüm formatlanmış verileri içerir.
 */
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
        data class ShowSnackbar(val message: UiText) : Effect
        data object TimerFinished : Effect
    }
}