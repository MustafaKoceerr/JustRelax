package com.mustafakoceerr.justrelax.feature.mixer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.domain.controller.SoundController
import com.mustafakoceerr.justrelax.core.domain.usecase.player.SetMixUseCase
import com.mustafakoceerr.justrelax.core.model.SoundUi
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.mixer.usecase.GenerateRandomMixUseCase
import justrelax.feature.mixer.generated.resources.Res
import justrelax.feature.mixer.generated.resources.err_mixer_generate_failed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MixerViewModel(
    private val generateRandomMixUseCase: GenerateRandomMixUseCase,
    private val setMixUseCase: SetMixUseCase,
    private val soundController: SoundController,
) : ViewModel() {

    private data class ScreenState(
        val selectedSoundCount: Int = DEFAULT_SOUND_COUNT,
        val isGenerating: Boolean = false,
        val mixedSounds: List<SoundUi> = emptyList(),
        val userMessage: UiText? = null,
    )

    private val screenState = MutableStateFlow(ScreenState())

    val uiState: StateFlow<MixerUiState> = combine(screenState, soundController.state) { screen, mixer ->
        MixerUiState(
            selectedSoundCount = screen.selectedSoundCount,
            isGenerating = screen.isGenerating,
            mixedSounds = screen.mixedSounds,
            // A paused mix shows its sounds as not playing.
            playingSoundIds = if (mixer.isPlaying) mixer.activeSounds.mapTo(mutableSetOf()) { it.id } else emptySet(),
            soundVolumes = mixer.activeSounds.associate { it.id to it.initialVolume },
            userMessage = screen.userMessage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MixerUiState())

    fun selectSoundCount(count: Int) =
        screenState.update { it.copy(selectedSoundCount = count.coerceIn(MIN_SOUND_COUNT, MAX_SOUND_COUNT)) }

    fun generateMix() {
        if (screenState.value.isGenerating) return
        screenState.update { it.copy(isGenerating = true) }

        viewModelScope.launch {
            val mix = generateRandomMixUseCase(screenState.value.selectedSoundCount)
            if (mix.isEmpty()) {
                screenState.update {
                    it.copy(
                        isGenerating = false,
                        mixedSounds = emptyList(),
                        userMessage = UiText.Resource(Res.string.err_mixer_generate_failed),
                    )
                }
                return@launch
            }

            soundController.setVolumes(mix.mapKeys { (sound, _) -> sound.id })
            setMixUseCase(mix)
            screenState.update { it.copy(isGenerating = false, mixedSounds = mix.keys.toList()) }
        }
    }

    fun toggleSound(soundId: String) {
        viewModelScope.launch { soundController.toggleSound(soundId) }
    }

    fun changeVolume(soundId: String, volume: Float) = soundController.changeVolume(soundId, volume)

    fun onMessageShown() = screenState.update { it.copy(userMessage = null) }

    companion object {
        const val DEFAULT_SOUND_COUNT = 4
        private const val MIN_SOUND_COUNT = 2
        private const val MAX_SOUND_COUNT = 7
    }
}
