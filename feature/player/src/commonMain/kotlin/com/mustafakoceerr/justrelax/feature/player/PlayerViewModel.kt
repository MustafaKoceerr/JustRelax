package com.mustafakoceerr.justrelax.feature.player

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.domain.usecase.player.GetGlobalMixerStateUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.StopAllSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.TogglePauseResumeUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.savedmix.SaveCurrentMixUseCase
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.player.mvi.PlayerContract
import justrelax.feature.player.generated.resources.Res
import justrelax.feature.player.generated.resources.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val soundRepository: SoundRepository,
    private val getGlobalMixerStateUseCase: GetGlobalMixerStateUseCase,
    private val togglePauseResumeUseCase: TogglePauseResumeUseCase,
    private val stopAllSoundsUseCase: StopAllSoundsUseCase,
    private val saveCurrentMixUseCase: SaveCurrentMixUseCase
) : ScreenModel {

    // 1. STATE
    private val _state = MutableStateFlow(PlayerContract.State())
    val state: StateFlow<PlayerContract.State> = _state.asStateFlow()

    // 2. EFFECT
    private val _effect = Channel<PlayerContract.Effect>()
    val effect: Flow<PlayerContract.Effect> = _effect.receiveAsFlow()

    init {
        observePlayerState()
    }

    private fun observePlayerState() {
        screenModelScope.launch {
            combine(
                soundRepository.getSounds(),
                getGlobalMixerStateUseCase()
            ) { allSounds, mixerState ->
                val activeSoundList = mixerState.activeSounds.mapNotNull { config ->
                    allSounds.find { it.id == config.id }
                }
                activeSoundList to mixerState.isPlaying
            }.collect { (activeSounds, isPlaying) ->
                _state.update { current ->
                    current.copy(
                        activeSounds = activeSounds,
                        isPlaying = isPlaying
                    )
                }
            }
        }
    }

    fun onEvent(event: PlayerContract.Event) {
        when (event) {
            PlayerContract.Event.StopAll -> {
                screenModelScope.launch { stopAllSoundsUseCase() }
            }

            PlayerContract.Event.ToggleMasterPlayPause -> {
                screenModelScope.launch { togglePauseResumeUseCase() }
            }

            PlayerContract.Event.OpenSaveDialog -> {
                _state.update { it.copy(isSaveDialogVisible = true) }
            }

            PlayerContract.Event.DismissSaveDialog -> {
                _state.update { it.copy(isSaveDialogVisible = false) }
            }

            is PlayerContract.Event.SaveMix -> {
                saveMix(event.name)
            }
        }
    }

    private fun saveMix(name: String) {
        screenModelScope.launch {
            // 1. Loading Başlat
            _state.update { it.copy(isSaving = true) }

            // 2. UseCase Çağır
            val result = saveCurrentMixUseCase(name)

            // 3. Loading Bitir ve Duruma Göre Diyaloğu Yönet
            _state.update {
                it.copy(
                    isSaving = false,
                    // Başarılıysa kapat, hataysa açık kalsın ki kullanıcı düzeltebilsin
                    isSaveDialogVisible = result !is Resource.Success
                )
            }

            // 4. Sonucu Kullanıcıya Bildir (Localization Uyumlu)
            when (result) {
                is Resource.Success -> {
                    _effect.send(
                        PlayerContract.Effect.ShowSnackbar(
                            // %1$s formatı ile mix ismini mesajın içine gömüyoruz
                            UiText.Resource(Res.string.msg_mix_saved_success, listOf(name))
                        )
                    )
                }

                is Resource.Error -> {
                    // Hata türüne göre doğru mesajı seç
                    val errorText = when (result.error) {
                        is AppError.SaveMix.EmptyName -> UiText.Resource(Res.string.err_mix_save_empty_name)
                        is AppError.SaveMix.NameAlreadyExists -> UiText.Resource(Res.string.err_mix_save_name_exists)
                        is AppError.SaveMix.NoSoundsPlaying -> UiText.Resource(Res.string.err_mix_save_no_sound)
                        else -> UiText.Resource(Res.string.err_unknown)
                    }
                    _effect.send(PlayerContract.Effect.ShowSnackbar(errorText))
                }

                else -> {}
            }
        }
    }
}