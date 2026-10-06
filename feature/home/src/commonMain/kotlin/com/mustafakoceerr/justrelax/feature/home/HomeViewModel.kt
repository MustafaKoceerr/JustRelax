package com.mustafakoceerr.justrelax.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import com.mustafakoceerr.justrelax.core.domain.usecase.player.PlaySoundUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadSingleSoundUseCase
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.home.domain.usecase.GetLocalizedCategorizedSoundsUseCase
import justrelax.feature.home.generated.resources.Res
import justrelax.feature.home.generated.resources.download_failed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    getLocalizedCategorizedSoundsUseCase: GetLocalizedCategorizedSoundsUseCase,
    private val audioMixer: AudioMixer,
    private val playSoundUseCase: PlaySoundUseCase,
    private val downloadSingleSoundUseCase: DownloadSingleSoundUseCase,
) : ViewModel() {

    private data class ScreenState(
        val selectedCategory: SoundCategory? = null,
        val downloadingSoundIds: Set<String> = emptySet(),
        val userMessage: UiText? = null,
    )

    private val screenState = MutableStateFlow(ScreenState())

    val uiState: StateFlow<HomeUiState> = combine(
        getLocalizedCategorizedSoundsUseCase(),
        audioMixer.state,
        screenState,
    ) { sounds, mixer, screen ->
        val categories = (sounds as? Resource.Success)?.data.orEmpty()
        HomeUiState(
            isLoading = sounds is Resource.Loading,
            categories = categories,
            selectedCategory = screen.selectedCategory ?: categories.keys.firstOrNull(),
            playingSoundIds = mixer.activeSounds.mapTo(mutableSetOf()) { it.id },
            soundVolumes = mixer.activeSounds.associate { it.id to it.initialVolume },
            downloadingSoundIds = screen.downloadingSoundIds,
            userMessage = screen.userMessage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun selectCategory(category: SoundCategory) = screenState.update { it.copy(selectedCategory = category) }

    /** Stops a playing sound; otherwise plays it, downloading it first if needed. */
    fun toggleSound(sound: LocalizedSound) {
        viewModelScope.launch {
            when {
                audioMixer.state.value.activeSounds.any { it.id == sound.id } -> audioMixer.stopSound(sound.id)
                sound.isDownloaded -> play(sound.id)
                else -> downloadAndPlay(sound)
            }
        }
    }

    fun changeVolume(soundId: String, volume: Float) = audioMixer.setVolume(soundId, volume)

    fun onMessageShown() = screenState.update { it.copy(userMessage = null) }

    private suspend fun play(soundId: String) {
        val result = playSoundUseCase(soundId)
        if (result is Resource.Error) {
            showMessage(UiText.DynamicString(result.error.message ?: "An unknown error occurred."))
        }
    }

    private suspend fun downloadAndPlay(sound: LocalizedSound) {
        screenState.update { it.copy(downloadingSoundIds = it.downloadingSoundIds + sound.id) }
        val downloaded = downloadSingleSoundUseCase(soundId = sound.id, remoteUrl = sound.remoteUrl)
        screenState.update { it.copy(downloadingSoundIds = it.downloadingSoundIds - sound.id) }

        if (downloaded) play(sound.id) else showMessage(UiText.Resource(Res.string.download_failed))
    }

    private fun showMessage(message: UiText) = screenState.update { it.copy(userMessage = message) }
}
