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
import com.mustafakoceerr.justrelax.core.data.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.err_mix_save_empty_name
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.err_mix_save_name_exists
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.err_mix_save_no_sound
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.err_unknown
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.msg_mix_saved_success
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
) : ViewModel() {

    private data class ScreenState(
        val isSaveDialogVisible: Boolean = false,
        val isSaving: Boolean = false,
        val userMessage: UiText? = null,
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
            isSaving = screen.isSaving,
            userMessage = screen.userMessage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SceneUiState())

    /** Kept out of [uiState], so dragging a slider redraws only that slider and not the panel. */
    val volumes: StateFlow<Map<String, Float>> = sceneRepository.scene
        .map { it.volumes }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val miniPlayer: StateFlow<MiniPlayerUiState> = uiState
        .map { scene -> MiniPlayerUiState(icons = scene.layers.map { it.icon }, isPlaying = scene.isPlaying) }
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

    fun openSaveDialog() = screenState.update { it.copy(isSaveDialogVisible = true) }

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

    private fun Resource<Unit>.toMessage(name: String): UiText? = when (this) {
        is Resource.Success -> UiText.Resource(Res.string.msg_mix_saved_success, listOf(name))
        is Resource.Error -> UiText.Resource(
            when (error) {
                is AppError.SaveMix.EmptyName -> Res.string.err_mix_save_empty_name
                is AppError.SaveMix.NameAlreadyExists -> Res.string.err_mix_save_name_exists
                is AppError.SaveMix.NoSoundsPlaying -> Res.string.err_mix_save_no_sound
                else -> Res.string.err_unknown
            }
        )
        Resource.Loading -> null
    }
}
