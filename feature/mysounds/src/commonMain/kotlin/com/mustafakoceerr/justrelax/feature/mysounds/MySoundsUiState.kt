package com.mustafakoceerr.justrelax.feature.mysounds

import com.mustafakoceerr.justrelax.core.media.ImportedAudio
import com.mustafakoceerr.justrelax.core.model.SoundIcon
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind

data class MySoundItem(
    val id: String,
    val name: String,
    val kind: SoundKind,
    val icon: SoundIcon,
    val durationMs: Long,
)

data class MySoundsUiState(
    val isLoading: Boolean = true,
    val sounds: List<MySoundItem> = emptyList(),
    val isImporting: Boolean = false,
    /** A file was imported; the screen opens the editor for it and calls `onEditorOpened`. */
    val editorToOpen: ImportedAudio? = null,
    /** Waiting for the user to confirm the deletion. */
    val soundToDelete: MySoundItem? = null,
    val userMessage: MySoundsMessage? = null,
)
