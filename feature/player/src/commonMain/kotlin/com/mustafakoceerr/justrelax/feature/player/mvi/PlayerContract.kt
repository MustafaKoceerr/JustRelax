package com.mustafakoceerr.justrelax.feature.player.mvi

import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.util.UiText

interface PlayerContract {

    data class State(
        val activeSounds: List<Sound> = emptyList(),
        val isPlaying: Boolean = false,
        val isSaveDialogVisible: Boolean = false,
        val isSaving: Boolean = false
    ) {
        val isVisible: Boolean
            get() = activeSounds.isNotEmpty()
    }

    sealed interface Event {
        data object ToggleMasterPlayPause : Event
        data object StopAll : Event
        data object OpenSaveDialog : Event
        data object DismissSaveDialog : Event
        data class SaveMix(val name: String) : Event
    }

    sealed interface Effect {
        data class ShowSnackbar(val message: UiText) : Effect
    }
}