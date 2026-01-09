package com.mustafakoceerr.justrelax.feature.mixer.mvi

import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.util.UiText

interface MixerContract {

    data class State(
        val isGenerating: Boolean = false,
        val selectedSoundCount: Int = 4,
        val mixedSounds: List<Sound> = emptyList()
    )

    sealed interface Event {
        data class SelectSoundCount(val count: Int) : Event
        data object GenerateMix : Event
        data class ToggleSound(val soundId: String) : Event
        data class ChangeVolume(val soundId: String, val volume: Float) : Event
    }

    sealed interface Effect {
        data class ShowSnackbar(val message: UiText) : Effect
    }
}