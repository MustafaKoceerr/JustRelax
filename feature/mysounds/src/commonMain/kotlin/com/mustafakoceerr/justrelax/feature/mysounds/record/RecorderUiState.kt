package com.mustafakoceerr.justrelax.feature.mysounds.record

import com.mustafakoceerr.justrelax.core.media.ImportedAudio
import com.mustafakoceerr.justrelax.core.ui.util.UiText

data class RecorderUiState(
    val isRecording: Boolean = false,
    val elapsedMs: Long = 0,
    /** The microphone's level, from 0 to 1, for the meter. */
    val level: Float = 0f,
    /** The recording is finished; the screen opens the editor for it and calls `onEditorOpened`. */
    val editorToOpen: ImportedAudio? = null,
    val userMessage: UiText? = null,
) {
    val maxMs: Long get() = RecorderViewModel.MAX_DURATION_MS
}
