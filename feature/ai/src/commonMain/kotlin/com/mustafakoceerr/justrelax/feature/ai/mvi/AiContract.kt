package com.mustafakoceerr.justrelax.feature.ai.mvi

import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.util.UiText

interface AiContract {

    data class State(
        val prompt: String = "",
        val isLoading: Boolean = false,
        val generatedMixName: String = "",
        val generatedMixDescription: String = "",
        val generatedSounds: List<Sound> = emptyList(),
        // todo: fetch from string resources
        val suggestions: List<String> = listOf(
            "Rainy Forest", "Deep Sleep", "Cafe Ambience", "Meditation"
        )
    )

    sealed interface Event {
        data class UpdatePrompt(val text: String) : Event
        data class SelectSuggestion(val text: String) : Event
        data object GenerateMix : Event
        data object RegenerateMix : Event
        data object EditPrompt : Event
        data object ClearMix : Event
        data class ToggleSound(val soundId: String) : Event
        data class ChangeVolume(val soundId: String, val volume: Float) : Event
    }

    sealed interface Effect {
        data class ShowSnackbar(val message: UiText) : Effect
    }
}