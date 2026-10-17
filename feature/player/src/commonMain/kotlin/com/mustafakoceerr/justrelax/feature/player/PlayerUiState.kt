package com.mustafakoceerr.justrelax.feature.player

import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.util.UiText

data class PlayerUiState(
    val activeSounds: List<Sound> = emptyList(),
    val isPlaying: Boolean = false,
    val isSaveDialogVisible: Boolean = false,
    val isSaving: Boolean = false,
    val userMessage: UiText? = null,
) {
    val isVisible: Boolean get() = activeSounds.isNotEmpty()
}
