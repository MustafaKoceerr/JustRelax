package com.mustafakoceerr.justrelax.feature.mixer

import com.mustafakoceerr.justrelax.core.presentation.SceneCards
import com.mustafakoceerr.justrelax.core.model.LocalizedSound

data class MixerUiState(
    val selectedSoundCount: Int = MixerViewModel.DEFAULT_SOUND_COUNT,
    val isGenerating: Boolean = false,
    val mixedSounds: List<LocalizedSound> = emptyList(),
    /** Which sounds play and how often, for the sound cards. Volumes come from the ViewModel's `volumes`. */
    val sceneCards: SceneCards = SceneCards(),
    /** Sounds that stay when the rest of the mix is shuffled again. */
    val lockedIds: Set<String> = emptySet(),
    val userMessage: MixerMessage? = null,
) {
    /** The mix can't be smaller than what is locked. */
    val minSelectableCount: Int get() = maxOf(RandomSceneGenerator.MIN_LAYERS, lockedIds.size)
}
