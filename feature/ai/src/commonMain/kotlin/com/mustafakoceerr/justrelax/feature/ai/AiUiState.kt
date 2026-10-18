package com.mustafakoceerr.justrelax.feature.ai

import com.mustafakoceerr.justrelax.core.ui.components.SceneCards
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.core.ui.util.UiText

data class AiUiState(
    val prompt: String = "",
    val isLoading: Boolean = false,
    val mixName: String = "",
    val mixDescription: String = "",
    val mixSounds: List<LocalizedSound> = emptyList(),
    /** Which sounds play and how often, for the sound cards. Volumes come from the ViewModel's `volumes`. */
    val sceneCards: SceneCards = SceneCards(),
    /** Asks once, before the first mix, whether the prompt may go to OpenAI. */
    val showAiDataNotice: Boolean = false,
    val userMessage: UiText? = null,
) {
    val hasResult: Boolean get() = mixSounds.isNotEmpty()
}
