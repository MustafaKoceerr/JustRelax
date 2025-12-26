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
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadSingleSoundUseCase
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
    private val downloadSingleSoundUseCase: DownloadSingleSoundUseCase, // DEĞİŞTİ
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
            is HomeIntent.ToggleSound -> toggleSound(intent.soundId)
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

    private fun toggleSound(soundId: String) {
        screenModelScope.launch {
            val isPlaying = _state.value.playingSoundIds.contains(soundId)

            if (isPlaying) {
                stopSoundUseCase(soundId)
                return@launch
            }

            if (checkMaxActiveSoundsUseCase()) {
                val limit = checkMaxActiveSoundsUseCase.getLimit()
                sendEffect(HomeEffect.ShowError(AppError.Player.LimitExceeded(limit)))
                return@launch
            }

            // soundId -> Sound (state’ten bul)
            val sound = _state.value.allSounds.firstOrNull { it.id == soundId }
            sound?.let {
                if (it.isDownloaded) {
                    playSound(soundId)
                } else {
                    downloadAndPlaySound(soundId)
                }
            }
        }
    }

    private fun downloadAndPlaySound(soundId: String) {
        screenModelScope.launch {
            // 1. İndirme için Sound objesine (URL'e) ihtiyacımız var.
            // Bunu zaten yüklü olan listeden ID ile buluyoruz.
            val sound = _state.value.allSounds.find { it.id == soundId } ?: return@launch

            // 2. Loading durumuna al
            _state.update { it.copy(downloadingSoundIds = it.downloadingSoundIds + soundId) }

            // 3. İndirmeyi başlat
            val isSuccess = downloadSingleSoundUseCase(sound)

            // 4. Loading'den çıkar
            _state.update { it.copy(downloadingSoundIds = it.downloadingSoundIds - soundId) }

            if (isSuccess) {
                // 5. Başarılıysa çal
                playSound(soundId)
            } else {
                // 6. Hata varsa bildir
                sendEffect(HomeEffect.ShowMessage("Download failed. Please check your connection."))
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