package com.mustafakoceerr.justrelax.feature.mixer

import com.mustafakoceerr.justrelax.core.ui.components.SceneCards
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.core.ui.util.UiText

data class MixerUiState(
    val selectedSoundCount: Int = MixerViewModel.DEFAULT_SOUND_COUNT,
    val isGenerating: Boolean = false,
    val mixedSounds: List<LocalizedSound> = emptyList(),
    /** Which sounds play, at what volume and how often, for the sound cards. */
    val sceneCards: SceneCards = SceneCards(),
    val userMessage: UiText? = null,
)
