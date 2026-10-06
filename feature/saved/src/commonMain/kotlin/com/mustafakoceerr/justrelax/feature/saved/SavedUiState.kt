package com.mustafakoceerr.justrelax.feature.saved

import com.mustafakoceerr.justrelax.core.ui.util.UiText

data class SavedMixItem(
    val id: Long,
    val title: String,
    val date: String,
    val icons: List<String>,
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
