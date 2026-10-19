package com.mustafakoceerr.justrelax.feature.scene

import com.mustafakoceerr.justrelax.core.analytics.Params
import com.mustafakoceerr.justrelax.core.analytics.EventTypes
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsEvent
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsHelper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.data.language.LanguageController
import com.mustafakoceerr.justrelax.core.data.repository.SavedSceneRepository
import com.mustafakoceerr.justrelax.core.data.repository.SceneRepository
import com.mustafakoceerr.justrelax.core.playback.PlaybackController
import com.mustafakoceerr.justrelax.core.playback.timer.SleepTimer
import com.mustafakoceerr.justrelax.core.analytics.logTimerStarted
import com.mustafakoceerr.justrelax.core.playback.timer.TimerStatus
import com.mustafakoceerr.justrelax.core.data.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The scene that is playing: the mini player and the scene panel with each layer's controls. */
class SceneViewModel(
    soundRepository: SoundRepository,
    private val sceneRepository: SceneRepository,
    private val playbackController: PlaybackController,
    private val savedSceneRepository: SavedSceneRepository,
    languageController: LanguageController,
    private val analytics: AnalyticsHelper,
    private val sleepTimer: SleepTimer,
) : ViewModel() {

    private data class ScreenState(
        val isSaveDialogVisible: Boolean = false,
        val saveDialogName: String = "",
        val isSaving: Boolean = false,
        val userMessage: SceneMessage? = null,
    )

    private val screenState = MutableStateFlow(ScreenState())

    val uiState: StateFlow<SceneUiState> = combine(
        soundRepository.getSounds(),
        sceneRepository.scene,
        playbackController.isPlaying,
        languageController.currentLanguage,
        screenState,
    ) { sounds, scene, isPlaying, language, screen ->
        val soundsById = sounds.associateBy { it.id }
        SceneUiState(
            layers = scene.layers.mapNotNull { layer ->
                val sound = soundsById[layer.soundId] ?: return@mapNotNull null
                SceneLayerItem(
                    soundId = layer.soundId,
                    name = sound.getDisplayName(language.code),
                    icon = sound.icon,
                    frequency = (layer as? Layer.Event)?.frequency,
                )
            },
            isPlaying = isPlaying,
            isSaveDialogVisible = screen.isSaveDialogVisible,
            saveDialogName = screen.saveDialogName,
            isSaving = screen.isSaving,
            userMessage = screen.userMessage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SceneUiState())

    /** Kept out of [uiState], so dragging a slider redraws only that slider and not the panel. */
    val volumes: StateFlow<Map<String, Float>> = sceneRepository.scene
        .map { it.volumes }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    /**
     * Seconds left on the sleep timer while it runs or is paused, else null. Kept apart from
     * [miniPlayer], so the tick each second redraws only the countdown.
     */
    val timerRemaining: StateFlow<Long?> = sleepTimer.state
        .map { timer -> timer.remainingSeconds.takeIf { timer.status != TimerStatus.IDLE } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val miniPlayer: StateFlow<MiniPlayerUiState> = uiState
        .map { scene ->
            MiniPlayerUiState(
                icons = scene.layers.map { it.icon },
                names = scene.layers.map { it.name },
                isPlaying = scene.isPlaying,
            )
        }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MiniPlayerUiState())

    fun togglePlayPause() {
        if (playbackController.isPlaying.value) playbackController.pause() else playbackController.play()
    }

    fun changeVolume(soundId: String, volume: Float) = sceneRepository.setVolume(soundId, volume)

    fun changeFrequency(soundId: String, frequency: EventFrequency) = sceneRepository.setFrequency(soundId, frequency)

    fun remove(soundId: String) {
        if (sceneRepository.scene.value.layer(soundId) == null) return
        viewModelScope.launch { sceneRepository.toggle(soundId) }
    }

    fun stopAll() {
        sceneRepository.clear()
    }

    /** A preset from the scene panel; the timer only makes sense while there is something to fade out. */
    fun startTimer(seconds: Long) {
        if (sceneRepository.scene.value.layers.isEmpty()) return
        sleepTimer.start(seconds)
        analytics.logTimerStarted(seconds)
    }

    fun cancelTimer() = sleepTimer.cancel()

    /** [suggestedName] fills the name field, such as the name the AI gave its mix. */
    fun openSaveDialog(suggestedName: String? = null) =
        screenState.update { it.copy(isSaveDialogVisible = true, saveDialogName = suggestedName.orEmpty()) }

    fun dismissSaveDialog() = screenState.update { it.copy(isSaveDialogVisible = false) }

    fun saveMix(name: String) {
        viewModelScope.launch {
            screenState.update { it.copy(isSaving = true) }
            val scene = sceneRepository.scene.value
            val result = savedSceneRepository.save(name, scene)
            if (result is Resource.Success) {
                analytics.logEvent(AnalyticsEvent(EventTypes.MIX_SAVED, mapOf(Params.LAYER_COUNT to scene.layers.size.toString())))
            }
            screenState.update {
                it.copy(
                    isSaving = false,
                    isSaveDialogVisible = result !is Resource.Success,
                    userMessage = result.toMessage(name),
                )
            }
        }
    }

    fun onMessageShown() = screenState.update { it.copy(userMessage = null) }

    private fun Resource<Unit>.toMessage(name: String): SceneMessage? = when (this) {
        is Resource.Success -> SceneMessage.Saved(name)
        is Resource.Error -> when (error) {
            is AppError.SaveMix.EmptyName -> SceneMessage.EmptyName
            is AppError.SaveMix.NameAlreadyExists -> SceneMessage.NameTaken
            is AppError.SaveMix.NoSoundsPlaying -> SceneMessage.NothingPlaying
            else -> SceneMessage.SaveFailed
        }
        Resource.Loading -> null
    }
}
