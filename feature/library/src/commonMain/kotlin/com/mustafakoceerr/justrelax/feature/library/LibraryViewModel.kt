package com.mustafakoceerr.justrelax.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.data.repository.SceneRepository
import com.mustafakoceerr.justrelax.core.playback.PlaybackController
import com.mustafakoceerr.justrelax.core.data.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.library.domain.usecase.GetLocalizedCategorizedSoundsUseCase
import justrelax.feature.library.generated.resources.Res
import justrelax.feature.library.generated.resources.download_failed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryViewModel(
    getLocalizedCategorizedSoundsUseCase: GetLocalizedCategorizedSoundsUseCase,
    private val sceneRepository: SceneRepository,
    private val playbackController: PlaybackController,
    private val soundRepository: SoundRepository,
) : ViewModel() {

    private data class ScreenState(
        val selectedCategory: SoundCategory? = null,
        val downloadingSoundIds: Set<String> = emptySet(),
        val userMessage: UiText? = null,
    )

    private val screenState = MutableStateFlow(ScreenState())

    val uiState: StateFlow<LibraryUiState> = combine(
        getLocalizedCategorizedSoundsUseCase(),
        sceneRepository.scene,
        screenState,
    ) { sounds, scene, screen ->
        val categories = (sounds as? Resource.Success)?.data.orEmpty()
        LibraryUiState(
            isLoading = sounds is Resource.Loading,
            categories = categories,
            selectedCategory = screen.selectedCategory ?: categories.keys.firstOrNull(),
            playingSoundIds = scene.layers.mapTo(mutableSetOf()) { it.soundId },
            soundVolumes = scene.layers.associate { it.soundId to it.volume },
            soundFrequencies = scene.events.associate { it.soundId to it.frequency },
            downloadingSoundIds = screen.downloadingSoundIds,
            userMessage = screen.userMessage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    fun selectCategory(category: SoundCategory) = screenState.update { it.copy(selectedCategory = category) }

    /** Stops a playing sound; otherwise plays it, downloading it first if needed. */
    fun toggleSound(sound: LocalizedSound) {
        viewModelScope.launch {
            when {
                sceneRepository.scene.value.layer(sound.id) != null -> sceneRepository.toggle(sound.id)
                sound.isDownloaded -> play(sound.id)
                else -> downloadAndPlay(sound)
            }
        }
    }

    fun changeVolume(soundId: String, volume: Float) = sceneRepository.setVolume(soundId, volume)

    fun changeFrequency(soundId: String, frequency: EventFrequency) = sceneRepository.setFrequency(soundId, frequency)

    fun onMessageShown() = screenState.update { it.copy(userMessage = null) }

    private suspend fun play(soundId: String) {
        when (val result = sceneRepository.toggle(soundId)) {
            is Resource.Error -> showMessage(UiText.DynamicString(result.error.message ?: "An unknown error occurred."))
            else -> playbackController.play()
        }
    }

    private suspend fun downloadAndPlay(sound: LocalizedSound) {
        screenState.update { it.copy(downloadingSoundIds = it.downloadingSoundIds + sound.id) }
        val downloaded = soundRepository.download(sound.id)
        screenState.update { it.copy(downloadingSoundIds = it.downloadingSoundIds - sound.id) }

        if (downloaded) play(sound.id) else showMessage(UiText.Resource(Res.string.download_failed))
    }

    private fun showMessage(message: UiText) = screenState.update { it.copy(userMessage = message) }
}
