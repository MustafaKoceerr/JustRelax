package com.mustafakoceerr.justrelax.feature.library

import com.mustafakoceerr.justrelax.core.presentation.SceneCards
import com.mustafakoceerr.justrelax.core.analytics.Params
import com.mustafakoceerr.justrelax.core.analytics.EventTypes
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsEvent
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsHelper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.data.repository.SceneRepository
import com.mustafakoceerr.justrelax.core.playback.PlaybackController
import com.mustafakoceerr.justrelax.core.data.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.data.repository.StarterPackInstaller
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.feature.library.domain.usecase.GetLocalizedCategorizedSoundsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryViewModel(
    getLocalizedCategorizedSoundsUseCase: GetLocalizedCategorizedSoundsUseCase,
    private val sceneRepository: SceneRepository,
    private val playbackController: PlaybackController,
    private val soundRepository: SoundRepository,
    private val starterPackInstaller: StarterPackInstaller,
    private val analytics: AnalyticsHelper,
) : ViewModel() {

    private data class ScreenState(
        val selectedCategory: SoundCategory? = null,
        val downloadingSoundIds: Set<String> = emptySet(),
        val userMessage: LibraryMessage? = null,
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
            sceneCards = SceneCards.of(scene),
            downloadingSoundIds = screen.downloadingSoundIds,
            userMessage = screen.userMessage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    /** Kept out of [uiState], so dragging a slider redraws only that slider and not the whole screen. */
    val volumes: StateFlow<Map<String, Float>> = sceneRepository.scene
        .map { it.volumes }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun selectCategory(category: SoundCategory) = screenState.update { it.copy(selectedCategory = category) }

    /** Stops a playing sound; otherwise plays it, downloading it first if needed. */
    fun toggleSound(sound: LocalizedSound) {
        viewModelScope.launch {
            when {
                sceneRepository.scene.value.layer(sound.id) != null -> sceneRepository.toggle(sound.id)
                sound.isDownloaded -> play(sound)
                else -> downloadAndPlay(sound)
            }
        }
    }

    fun changeVolume(soundId: String, volume: Float) = sceneRepository.setVolume(soundId, volume)

    fun changeFrequency(soundId: String, frequency: EventFrequency) = sceneRepository.setFrequency(soundId, frequency)

    /** Fetches the catalog again and resumes the starter pack after a failed first launch. */
    fun retry() {
        viewModelScope.launch {
            soundRepository.sync()
            starterPackInstaller.install()
        }
    }

    fun onMessageShown() = screenState.update { it.copy(userMessage = null) }

    private suspend fun play(sound: LocalizedSound) {
        when (val result = sceneRepository.toggle(sound.id)) {
            is Resource.Error -> showMessage(LibraryMessage.Text(result.error.message ?: "An unknown error occurred."))
            else -> {
                playbackController.play()
                analytics.logEvent(
                    AnalyticsEvent(
                        EventTypes.SOUND_PLAY,
                        mapOf(Params.SOUND_ID to sound.id, Params.CATEGORY to sound.category.name, Params.SOURCE to sound.source.name),
                    ),
                )
            }
        }
    }

    private suspend fun downloadAndPlay(sound: LocalizedSound) {
        screenState.update { it.copy(downloadingSoundIds = it.downloadingSoundIds + sound.id) }
        val downloaded = soundRepository.download(sound.id)
        screenState.update { it.copy(downloadingSoundIds = it.downloadingSoundIds - sound.id) }

        if (downloaded) play(sound) else showMessage(LibraryMessage.DownloadFailed)
    }

    private fun showMessage(message: LibraryMessage) = screenState.update { it.copy(userMessage = message) }
}
