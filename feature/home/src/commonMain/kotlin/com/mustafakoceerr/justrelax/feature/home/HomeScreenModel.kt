package com.mustafakoceerr.justrelax.feature.home


import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.usecase.player.AdjustVolumeUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.CheckMaxActiveSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.GetPlayingSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.PlaySoundUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.player.StopSoundUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.GetSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadSoundUseCase
import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeEffect
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeIntent
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeScreenModel(
    private val getSoundsUseCase: GetSoundsUseCase,
    private val downloadSoundUseCase: DownloadSoundUseCase,
    private val playSoundUseCase: PlaySoundUseCase,
    private val stopSoundUseCase: StopSoundUseCase,
    private val adjustVolumeUseCase: AdjustVolumeUseCase,
    private val getPlayingSoundsUseCase: GetPlayingSoundsUseCase,
    private val checkMaxActiveSoundsUseCase: CheckMaxActiveSoundsUseCase
) : ScreenModel {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = Channel<HomeEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        observeSounds()
        observePlayingState()
    }

    fun processIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.SelectCategory -> selectCategory(intent.category)
            is HomeIntent.ToggleSound -> toggleSound(intent.sound)
            is HomeIntent.ChangeVolume -> changeVolume(intent.soundId, intent.volume)
            HomeIntent.SettingsClicked -> sendEffect(HomeEffect.NavigateToSettings)
        }
    }

    private fun observeSounds() {
        getSoundsUseCase()
            .onEach { sounds ->
                _state.update {
                    it.copy(
                        allSounds = sounds,
                        filteredSounds = sounds.filter { s -> s.categoryId == it.selectedCategory.id },
                        isLoading = false
                    )
                }
            }
            .launchIn(screenModelScope)
    }

    private fun observePlayingState() {
        getPlayingSoundsUseCase()
            .onEach { playingIds ->
                _state.update { it.copy(playingSoundIds = playingIds) }
            }
            .launchIn(screenModelScope)
    }

    private fun selectCategory(category: com.mustafakoceerr.justrelax.core.model.SoundCategory) {
        _state.update {
            it.copy(
                selectedCategory = category,
                filteredSounds = it.allSounds.filter { s -> s.categoryId == category.id }
            )
        }
    }

    private fun toggleSound(sound: Sound) {
        screenModelScope.launch {
            val isPlaying = _state.value.playingSoundIds.contains(sound.id)

            if (isPlaying) {
                stopSoundUseCase(sound.id)
            } else {
                if (checkMaxActiveSoundsUseCase()) {
                    val limit = checkMaxActiveSoundsUseCase.getLimit()
                    sendEffect(HomeEffect.ShowError(AppError.Player.LimitExceeded(limit)))
                    return@launch
                }

                if (sound.isDownloaded) {
                    playSound(sound.id)
                } else {
                    downloadAndPlaySound(sound.id)
                }
            }
        }
    }

    private fun downloadAndPlaySound(soundId: String) {
        screenModelScope.launch {
            _state.update { it.copy(downloadingSoundIds = it.downloadingSoundIds + soundId) }

            downloadSoundUseCase(soundId).collect { status ->
                when (status) {
                    is DownloadStatus.Completed -> {
                        // İndirme bitti, loading'den düş
                        _state.update { it.copy(downloadingSoundIds = it.downloadingSoundIds - soundId) }
                        // ŞİMDİ ÇALMAK GÜVENLİ
                        playSound(soundId)
                    }
                    is DownloadStatus.Error -> {
                        _state.update { it.copy(downloadingSoundIds = it.downloadingSoundIds - soundId) }
                        sendEffect(HomeEffect.ShowMessage(status.message))
                    }
                    else -> {} // Progress vs.
                }
            }
        }
    }

    private suspend fun playSound(soundId: String) {
        val currentVolume = _state.value.soundVolumes[soundId] ?: 0.5f
        val result = playSoundUseCase(soundId, volume = currentVolume)

        if (result is Resource.Error) {
            sendEffect(HomeEffect.ShowError(result.error))
        }
    }

    private fun changeVolume(soundId: String, volume: Float) {
        _state.update {
            val newVolumes = it.soundVolumes.toMutableMap().apply { put(soundId, volume) }
            it.copy(soundVolumes = newVolumes)
        }
        adjustVolumeUseCase(soundId, volume)
    }

    private fun sendEffect(effect: HomeEffect) {
        screenModelScope.launch { _effect.send(effect) }
    }
}