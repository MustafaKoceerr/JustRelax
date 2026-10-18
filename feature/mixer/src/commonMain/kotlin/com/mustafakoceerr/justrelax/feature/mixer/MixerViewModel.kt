package com.mustafakoceerr.justrelax.feature.mixer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.data.language.LanguageController
import com.mustafakoceerr.justrelax.core.data.repository.SceneRepository
import com.mustafakoceerr.justrelax.core.data.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.core.model.localized
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.playback.PlaybackController
import com.mustafakoceerr.justrelax.core.playback.toggleAndPlay
import com.mustafakoceerr.justrelax.core.ui.components.SceneCards
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.mixer.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.mixer.generated.resources.err_mixer_generate_failed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MixerViewModel(
    private val soundRepository: SoundRepository,
    private val sceneRepository: SceneRepository,
    private val languageController: LanguageController,
    private val playbackController: PlaybackController,
    private val sceneGenerator: RandomSceneGenerator,
) : ViewModel() {

    private data class ScreenState(
        val selectedSoundCount: Int = DEFAULT_SOUND_COUNT,
        val isGenerating: Boolean = false,
        val mixedSounds: List<LocalizedSound> = emptyList(),
        val userMessage: UiText? = null,
    )

    private val screenState = MutableStateFlow(ScreenState())

    val uiState: StateFlow<MixerUiState> = combine(screenState, sceneRepository.scene, playbackController.isPlaying) { screen, scene, isPlaying ->
        MixerUiState(
            selectedSoundCount = screen.selectedSoundCount,
            isGenerating = screen.isGenerating,
            mixedSounds = screen.mixedSounds,
            // A paused mix shows its sounds as not playing.
            sceneCards = SceneCards.of(scene, showPlaying = isPlaying),
            userMessage = screen.userMessage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MixerUiState())

    fun selectSoundCount(count: Int) = screenState.update {
        it.copy(selectedSoundCount = count.coerceIn(RandomSceneGenerator.MIN_LAYERS, RandomSceneGenerator.MAX_LAYERS))
    }

    fun generateMix() {
        if (screenState.value.isGenerating) return
        screenState.update { it.copy(isGenerating = true) }

        viewModelScope.launch {
            val sounds = soundRepository.getSounds().first()
            val scene = sceneGenerator.generate(sounds, screenState.value.selectedSoundCount)
            if (scene.layers.isEmpty()) {
                screenState.update {
                    it.copy(
                        isGenerating = false,
                        mixedSounds = emptyList(),
                        userMessage = UiText.Resource(Res.string.err_mixer_generate_failed),
                    )
                }
                return@launch
            }

            sceneRepository.replaceWith(scene)
            playbackController.play()
            val soundsById = sounds.associateBy { it.id }
            val languageCode = languageController.getCurrentLanguage().code
            screenState.update {
                it.copy(
                    isGenerating = false,
                    mixedSounds = scene.layers.mapNotNull { layer -> soundsById[layer.soundId]?.localized(languageCode) },
                )
            }
        }
    }

    fun toggleSound(soundId: String) {
        viewModelScope.launch { sceneRepository.toggleAndPlay(soundId, playbackController) }
    }

    fun changeVolume(soundId: String, volume: Float) = sceneRepository.setVolume(soundId, volume)

    fun changeFrequency(soundId: String, frequency: EventFrequency) = sceneRepository.setFrequency(soundId, frequency)

    fun onMessageShown() = screenState.update { it.copy(userMessage = null) }

    companion object {
        const val DEFAULT_SOUND_COUNT = 3
    }
}
