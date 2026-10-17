package com.mustafakoceerr.justrelax.feature.freesound

import com.mustafakoceerr.justrelax.core.model.FreesoundSound
import com.mustafakoceerr.justrelax.core.model.SoundOrigin
import com.mustafakoceerr.justrelax.core.ui.util.UiText

/** A downloaded preview to open in the editor, with the credit the saved sound keeps. */
data class FreesoundEditorRequest(val path: String, val name: String, val origin: SoundOrigin)

data class FreesoundUiState(
    val query: String = "",
    val results: List<FreesoundSound> = emptyList(),
    val hasSearched: Boolean = false,
    val isSearching: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    /** The result whose preview is playing. */
    val previewingId: Long? = null,
    val downloadingId: Long? = null,
    val editorToOpen: FreesoundEditorRequest? = null,
    val userMessage: UiText? = null,
)
