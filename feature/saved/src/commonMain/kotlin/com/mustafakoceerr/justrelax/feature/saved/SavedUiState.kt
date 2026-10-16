package com.mustafakoceerr.justrelax.feature.saved

import com.mustafakoceerr.justrelax.core.model.SoundIcon
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import kotlin.time.Instant

data class SavedMixItem(
    val id: Long,
    val title: String,
    val date: Instant,
    val icons: List<SoundIcon>,
)

/** A one-off snackbar message, optionally with an action (e.g. undo). */
data class SavedMessage(
    val text: UiText,
    val actionLabel: UiText? = null,
)

data class SavedUiState(
    val isLoading: Boolean = true,
    val mixes: List<SavedMixItem> = emptyList(),
    val message: SavedMessage? = null,
)
