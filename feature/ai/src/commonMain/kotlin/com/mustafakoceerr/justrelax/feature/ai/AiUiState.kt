package com.mustafakoceerr.justrelax.feature.ai

import com.mustafakoceerr.justrelax.core.model.SoundUi
import com.mustafakoceerr.justrelax.core.ui.util.UiText

data class AiUiState(
    val prompt: String = "",
    val isLoading: Boolean = false,
    val mixName: String = "",
    val mixDescription: String = "",
    val mixSounds: List<SoundUi> = emptyList(),
    val playingSoundIds: Set<String> = emptySet(),
    val soundVolumes: Map<String, Float> = emptyMap(),
    val userMessage: UiText? = null,
) {
    val hasResult: Boolean get() = mixSounds.isNotEmpty()
}
