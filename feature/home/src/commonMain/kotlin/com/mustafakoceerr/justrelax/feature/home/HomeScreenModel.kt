package com.mustafakoceerr.justrelax.feature.home


import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.usecase.player.AdjustVolumeUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.GetGlobalMixerStateUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.PlaySoundUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.StopSoundUseCase
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.home.domain.usecase.GetCategorizedSoundsUseCase
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeContract
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeScreenModel(
    getCategorizedSoundsUseCase: GetCategorizedSoundsUseCase,
    getGlobalMixerStateUseCase: GetGlobalMixerStateUseCase,
    private val playSoundUseCase: PlaySoundUseCase,
    private val stopSoundUseCase: StopSoundUseCase,
    private val adjustVolumeUseCase: AdjustVolumeUseCase
) : ScreenModel, HomeContract {

    private val _state = MutableStateFlow(HomeContract.State())
    val state = _state.asStateFlow()

    private val _effect = Channel<HomeContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        combine(
            getCategorizedSoundsUseCase(),
            getGlobalMixerStateUseCase()
        ) { soundsResult, playerState ->
            _state.update { currentState ->
                val categories = (soundsResult as? Resource.Success)?.data ?: currentState.categories

                // Eğer henüz bir kategori seçilmemişse ve kategoriler yeni geldiyse,
                // ilk kategoriyi varsayılan olarak seç.
                val selectedCategory = currentState.selectedCategory
                    ?: categories.keys.firstOrNull()

                currentState.copy(
                    isLoading = soundsResult is Resource.Loading,
                    categories = categories,
                    selectedCategory = selectedCategory,
                    playerState = playerState
                )
            }
        }.launchIn(screenModelScope)
    }

    fun onEvent(event: HomeContract.Event) {
        when (event) {
            // YENİ: Kategori seçme event'ini işle
            is HomeContract.Event.OnCategorySelected -> {
                _state.update { it.copy(selectedCategory = event.category) }
            }
            is HomeContract.Event.OnSoundClick -> handleSoundClick(event.sound)
            is HomeContract.Event.OnVolumeChange -> adjustVolumeUseCase(event.soundId, event.volume)
            is HomeContract.Event.OnSettingsClick -> sendEffect(HomeContract.Effect.NavigateToSettings)
        }
    }

    private fun handleSoundClick(sound: Sound) {
        screenModelScope.launch {
            val isPlaying = state.value.playerState.activeSounds.any { it.id == sound.id }

            if (isPlaying) {
                stopSoundUseCase(sound.id)
            } else {
                when (val result = playSoundUseCase(sound.id)) {
                    is Resource.Error -> {
                        val errorMessage = UiText.DynamicString(result.error.message ?: "An unknown error occurred.")
                        sendEffect(HomeContract.Effect.ShowSnackbar(errorMessage))
                    }
                    else -> { /* Başarılı */ }
                }
            }
        }
    }

    private fun sendEffect(effectToSend: HomeContract.Effect) {
        screenModelScope.launch {
            _effect.send(effectToSend)
        }
    }
}