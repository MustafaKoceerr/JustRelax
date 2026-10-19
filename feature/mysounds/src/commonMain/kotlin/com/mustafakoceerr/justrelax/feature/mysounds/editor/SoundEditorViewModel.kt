package com.mustafakoceerr.justrelax.feature.mysounds.editor

import com.mustafakoceerr.justrelax.core.analytics.CrashReporter
import com.mustafakoceerr.justrelax.core.analytics.Params
import com.mustafakoceerr.justrelax.core.analytics.EventTypes
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsEvent
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsHelper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.data.repository.UserSoundDraft
import com.mustafakoceerr.justrelax.core.data.repository.UserSoundRepository
import com.mustafakoceerr.justrelax.core.media.ClipPreviewer
import com.mustafakoceerr.justrelax.core.media.MediaException
import com.mustafakoceerr.justrelax.core.media.TrimRange
import com.mustafakoceerr.justrelax.core.media.TrimRules
import com.mustafakoceerr.justrelax.core.media.WaveformReader
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.SoundIcon
import com.mustafakoceerr.justrelax.core.model.SoundOrigin
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.feature.mysounds.editor.SoundEditorUiState.Companion.MAX_NAME_LENGTH
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Cuts a sound out of an imported file: the user drags the start and end over its waveform,
 * listens to the range, describes the sound and saves it to the library.
 */
class SoundEditorViewModel(
    private val sourcePath: String,
    suggestedName: String?,
    /** Where the file came from if it was found online; it decides the sound's license and credit. */
    private val origin: SoundOrigin?,
    private val waveformReader: WaveformReader,
    private val previewer: ClipPreviewer,
    private val userSoundRepository: UserSoundRepository,
    private val analytics: AnalyticsHelper,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val editorState = MutableStateFlow(
        SoundEditorUiState(name = suggestedName.orEmpty().substringBeforeLast('.').trim().take(MAX_NAME_LENGTH)),
    )

    val uiState: StateFlow<SoundEditorUiState> = combine(editorState, previewer.isPlaying) { state, isPlaying ->
        state.copy(isPreviewing = isPlaying)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), editorState.value)

    init {
        viewModelScope.launch { load() }
    }

    fun moveStart(toMs: Long) = editRange { state ->
        TrimRules.moveStart(state.range, toMs, state.kind, state.sourceMs)
    }

    fun moveEnd(toMs: Long) = editRange { state ->
        TrimRules.moveEnd(state.range, toMs, state.kind, state.sourceMs)
    }

    fun selectKind(kind: SoundKind) {
        if (kind !in editorState.value.allowedKinds) return
        previewer.stop()
        editorState.update { it.copy(kind = kind, range = TrimRules.fit(it.range, kind, it.sourceMs)) }
    }

    fun changeName(name: String) = editorState.update { it.copy(name = name.take(MAX_NAME_LENGTH)) }

    fun selectCategory(category: SoundCategory) = editorState.update { it.copy(category = category) }

    fun selectIcon(icon: SoundIcon) = editorState.update { it.copy(icon = icon) }

    fun togglePreview() {
        val state = editorState.value
        if (previewer.isPlaying.value) {
            previewer.stop()
        } else if (!state.isLoading && !state.loadFailed) {
            previewer.play(sourcePath, state.range, loop = state.kind == SoundKind.BED)
        }
    }

    fun save() {
        val state = editorState.value
        val category = state.category
        if (!state.canSave || category == null) return
        previewer.stop()
        editorState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val draft = UserSoundDraft(
                name = state.name,
                category = category,
                icon = state.icon ?: category.defaultIcon,
                kind = state.kind,
                sourcePath = sourcePath,
                range = state.range,
                origin = origin,
            )
            try {
                userSoundRepository.create(draft)
                analytics.logEvent(
                    AnalyticsEvent(
                        EventTypes.SOUND_IMPORTED,
                        mapOf(Params.KIND to draft.kind.name, Params.SOURCE to if (origin == null) "file" else "freesound"),
                    ),
                )
                editorState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: MediaException) {
                crashReporter.recordException(e)
                editorState.update {
                    it.copy(isSaving = false, userMessage = SoundEditorMessage.SaveFailed)
                }
            }
        }
    }

    fun onMessageShown() = editorState.update { it.copy(userMessage = null) }

    override fun onCleared() {
        previewer.release()
    }

    private suspend fun load() {
        try {
            val waveform = waveformReader.read(sourcePath, WAVEFORM_BUCKETS)
            val kind = TrimRules.defaultKind(waveform.durationMs)
            editorState.update {
                it.copy(
                    isLoading = false,
                    peaks = waveform.peaks,
                    sourceMs = waveform.durationMs,
                    kind = kind,
                    allowedKinds = TrimRules.allowedKinds(waveform.durationMs),
                    range = TrimRules.defaultRange(kind, waveform.durationMs),
                )
            }
        } catch (e: MediaException) {
            editorState.update { it.copy(isLoading = false, loadFailed = true) }
        }
    }

    /** A new range is heard only when the user plays it again. */
    private fun editRange(change: (SoundEditorUiState) -> TrimRange) {
        if (editorState.value.isLoading || editorState.value.loadFailed) return
        previewer.stop()
        editorState.update { it.copy(range = change(it)) }
    }

    private companion object {
        const val WAVEFORM_BUCKETS = 200
    }
}
