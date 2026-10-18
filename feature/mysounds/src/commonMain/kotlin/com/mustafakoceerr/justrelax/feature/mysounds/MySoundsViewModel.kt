package com.mustafakoceerr.justrelax.feature.mysounds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.data.repository.UserSoundRepository
import com.mustafakoceerr.justrelax.core.media.AudioImporter
import com.mustafakoceerr.justrelax.core.media.ImportedAudio
import com.mustafakoceerr.justrelax.core.media.MediaException
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.mysounds.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.mysounds.generated.resources.mysounds_import_failed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MySoundsViewModel(
    private val userSoundRepository: UserSoundRepository,
    private val importer: AudioImporter,
) : ViewModel() {

    private data class ScreenState(
        val isImporting: Boolean = false,
        val editorToOpen: ImportedAudio? = null,
        val soundToDeleteId: String? = null,
        val userMessage: UiText? = null,
    )

    private val screenState = MutableStateFlow(ScreenState())

    val uiState: StateFlow<MySoundsUiState> = combine(
        userSoundRepository.getUserSounds(),
        screenState,
    ) { sounds, screen ->
        val items = sounds.map { it.asItem() }
        MySoundsUiState(
            isLoading = false,
            sounds = items,
            isImporting = screen.isImporting,
            editorToOpen = screen.editorToOpen,
            soundToDelete = items.find { it.id == screen.soundToDeleteId },
            userMessage = screen.userMessage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MySoundsUiState())

    /** The document the user picked, or null if the picker was cancelled. */
    fun import(uri: String?) {
        if (uri == null) return
        viewModelScope.launch {
            screenState.update { it.copy(isImporting = true) }
            try {
                val imported = importer.import(uri)
                screenState.update { it.copy(isImporting = false, editorToOpen = imported) }
            } catch (e: MediaException) {
                screenState.update {
                    it.copy(isImporting = false, userMessage = UiText.Resource(Res.string.mysounds_import_failed))
                }
            }
        }
    }

    fun onEditorOpened() = screenState.update { it.copy(editorToOpen = null) }

    fun requestDelete(soundId: String) = screenState.update { it.copy(soundToDeleteId = soundId) }

    fun dismissDelete() = screenState.update { it.copy(soundToDeleteId = null) }

    fun confirmDelete() {
        val soundId = screenState.value.soundToDeleteId ?: return
        screenState.update { it.copy(soundToDeleteId = null) }
        viewModelScope.launch { userSoundRepository.delete(soundId) }
    }

    fun onMessageShown() = screenState.update { it.copy(userMessage = null) }

    private fun Sound.asItem() = MySoundItem(
        id = id,
        name = names.values.firstOrNull().orEmpty(),
        kind = kind,
        icon = icon,
        durationMs = clips.sumOf { it.durationMs },
    )
}
