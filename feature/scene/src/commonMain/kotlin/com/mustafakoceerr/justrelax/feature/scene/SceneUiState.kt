package com.mustafakoceerr.justrelax.feature.scene

import com.mustafakoceerr.justrelax.core.model.SoundIcon
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency

data class SceneUiState(
    val layers: List<SceneLayerItem> = emptyList(),
    val isPlaying: Boolean = false,
    val isSaveDialogVisible: Boolean = false,
    /** What the save dialog's name field starts with, such as an AI mix's name. */
    val saveDialogName: String = "",
    val isSaving: Boolean = false,
    val userMessage: SceneMessage? = null,
) {
    val isVisible: Boolean get() = layers.isNotEmpty()
}

/**
 * One sound of the scene; [frequency] is null for a bed, which simply loops. Its volume is in
 * [SceneViewModel.volumes].
 */
data class SceneLayerItem(
    val soundId: String,
    val name: String,
    val icon: SoundIcon,
    val frequency: EventFrequency?,
)

/** What the mini player shows. Volumes are left out, so dragging a slider does not redraw it. */
data class MiniPlayerUiState(
    val icons: List<SoundIcon> = emptyList(),
    /** In the app's language, in the scene's order. */
    val names: List<String> = emptyList(),
    val isPlaying: Boolean = false,
) {
    val isVisible: Boolean get() = icons.isNotEmpty()
}
