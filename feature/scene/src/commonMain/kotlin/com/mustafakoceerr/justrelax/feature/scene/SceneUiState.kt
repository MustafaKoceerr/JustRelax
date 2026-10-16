package com.mustafakoceerr.justrelax.feature.scene

import com.mustafakoceerr.justrelax.core.model.SoundIcon
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.ui.util.UiText

data class SceneUiState(
    val layers: List<SceneLayerItem> = emptyList(),
    val isPlaying: Boolean = false,
    val isSaveDialogVisible: Boolean = false,
    val isSaving: Boolean = false,
    val userMessage: UiText? = null,
) {
    val isVisible: Boolean get() = layers.isNotEmpty()
}

/** One sound of the scene; [frequency] is null for a bed, which simply loops. */
data class SceneLayerItem(
    val soundId: String,
    val name: String,
    val icon: SoundIcon,
    val volume: Float,
    val frequency: EventFrequency?,
)
