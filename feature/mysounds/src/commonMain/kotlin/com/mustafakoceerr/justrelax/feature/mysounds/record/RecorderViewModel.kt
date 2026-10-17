package com.mustafakoceerr.justrelax.feature.mysounds.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.media.AudioRecorder
import com.mustafakoceerr.justrelax.core.media.ImportedAudio
import com.mustafakoceerr.justrelax.core.media.MediaException
import com.mustafakoceerr.justrelax.core.playback.PlaybackController
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import justrelax.feature.mysounds.generated.resources.Res
import justrelax.feature.mysounds.generated.resources.mysounds_record_failed
import justrelax.feature.mysounds.generated.resources.mysounds_record_permission_denied
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

/**
 * Records a sound with the microphone, then hands the file to the editor. The scene pauses while
 * recording. Leaving the app finishes the recording, so no microphone foreground service is needed.
 */
class RecorderViewModel(
    private val recorder: AudioRecorder,
    private val playbackController: PlaybackController,
) : ViewModel() {

    private val state = MutableStateFlow(RecorderUiState())
    val uiState: StateFlow<RecorderUiState> = state.asStateFlow()

    private var recordingPath: String? = null
    private var ticker: Job? = null

    /** The answer to the microphone permission request; recording starts when it is granted. */
    fun onPermissionResult(granted: Boolean) {
        if (!granted) {
            showMessage(Res.string.mysounds_record_permission_denied)
            return
        }
        if (recordingPath != null) return
        try {
            recordingPath = recorder.start()
        } catch (e: MediaException) {
            showMessage(Res.string.mysounds_record_failed)
            return
        }
        playbackController.pause()
        state.update { it.copy(isRecording = true, elapsedMs = 0, level = 0f) }
        ticker = viewModelScope.launch {
            while (state.value.elapsedMs < MAX_DURATION_MS) {
                delay(TICK_MS)
                state.update { it.copy(elapsedMs = it.elapsedMs + TICK_MS, level = recorder.level()) }
            }
            finish()
        }
    }

    fun stop() {
        if (recordingPath != null) finish()
    }

    /** The app went to the background. */
    fun onAppStopped() = stop()

    fun discard() {
        if (recordingPath == null) return
        ticker?.cancel()
        recorder.cancel()
        recordingPath = null
        state.update { it.copy(isRecording = false, elapsedMs = 0, level = 0f) }
    }

    fun onEditorOpened() = state.update { it.copy(editorToOpen = null) }

    fun onMessageShown() = state.update { it.copy(userMessage = null) }

    override fun onCleared() {
        discard()
    }

    private fun finish() {
        val path = recordingPath ?: return
        recordingPath = null
        ticker?.cancel()
        try {
            recorder.stop()
            state.update { it.copy(isRecording = false, level = 0f, editorToOpen = ImportedAudio(path, null)) }
        } catch (e: MediaException) {
            state.update { it.copy(isRecording = false, level = 0f) }
            showMessage(Res.string.mysounds_record_failed)
        }
    }

    private fun showMessage(message: StringResource) =
        state.update { it.copy(userMessage = UiText.Resource(message)) }

    companion object {
        const val MAX_DURATION_MS = 5 * 60 * 1_000L
        private const val TICK_MS = 100L
    }
}
