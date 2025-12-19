package com.mustafakoceerr.justrelax.feature.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.audio.SoundManager
import com.mustafakoceerr.justrelax.core.audio.domain.usecase.ToggleSoundUseCase
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeEffect
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeIntent
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeState
import com.mustafakoceerr.justrelax.feature.home.usecase.HomeBannerUseCases
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Todo: Ses açma kapatma ve ses yüksekliğini ayarlama işlerini SoundListController'a verebilirsin.
class HomeScreenModel(
    private val soundRepository: SoundRepository,
    private val soundManager: SoundManager,
    private val toggleSoundUseCase: ToggleSoundUseCase,
    private val bannerUseCases: HomeBannerUseCases
) : ScreenModel {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = Channel<HomeEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        // 1. Veritabanından sesleri dinle
        observeSounds()
        // 2. SoundManager'dan çalan sesleri dinle
        observeActiveSounds()
    }

    private fun observeSounds() {
        screenModelScope.launch {
            soundRepository.getSounds().collectLatest { sounds ->
                // Banner kontrolü (Yeni veri geldiğinde tekrar kontrol et)
                val shouldShow = bannerUseCases.shouldShow(sounds)

                _state.update {
                    it.copy(
                        allSounds = sounds,
                        showDownloadBanner = shouldShow && !it.isDownloadingAll, // İndiriliyorsa gösterme
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun observeActiveSounds() {
        screenModelScope.launch {
            soundManager.state.collectLatest { mixerState ->
                _state.update { currentState ->
                    // MixerState -> Map<Id, Volume> dönüşümü
                    val activeMap = mixerState.activeSounds.mapValues { it.value.targetVolume }
                    currentState.copy(activeSounds = activeMap)
                }
            }
        }
    }

    fun processIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.SelectCategory -> {
                _state.update { it.copy(selectedCategory = intent.category) }
            }

            is HomeIntent.ToggleSound -> toggleSound(intent)

            is HomeIntent.ChangeVolume -> {
                soundManager.onVolumeChange(intent.soundId, intent.volume)
            }

            HomeIntent.DownloadAllMissing -> downloadAllMissing()

            HomeIntent.DismissBanner -> dismissBanner()

            HomeIntent.SettingsClicked -> {
                screenModelScope.launch { _effect.send(HomeEffect.NavigateToSettings) }
            }
        }
    }

    private fun toggleSound(intent: HomeIntent.ToggleSound) {
        screenModelScope.launch {
            val isDownloading = _state.value.downloadingSoundIds.contains(intent.sound.id)

            toggleSoundUseCase(intent.sound, isDownloading).collect { result ->
                when (result) {
                    is ToggleSoundUseCase.Result.Downloading -> {
                        _state.update {
                            val newSet = if (result.isDownloading) {
                                it.downloadingSoundIds + intent.sound.id
                            } else {
                                it.downloadingSoundIds - intent.sound.id
                            }
                            it.copy(downloadingSoundIds = newSet)
                        }
                    }
                    is ToggleSoundUseCase.Result.Error -> {
                        _effect.send(HomeEffect.ShowMessage(result.message))
                    }
                    else -> {} // Toggled ve Ignored durumlarında UI zaten güncellenir
                }
            }
        }
    }

    private fun downloadAllMissing() {
        screenModelScope.launch {
            bannerUseCases.downloadAllMissingSounds().collect { status ->
                when (status) {
                    is BatchDownloadStatus.Progress -> {
                        _state.update {
                            it.copy(
                                isDownloadingAll = true,
                                totalDownloadProgress = status.percentage
                            )
                        }
                    }
                    BatchDownloadStatus.Completed -> {
                        _state.update {
                            it.copy(
                                isDownloadingAll = false,
                                showDownloadBanner = false // İşlem bitti, kapat
                            )
                        }
                        bannerUseCases.dismiss() // Bir daha sorma (Tarih güncelle)
                        // todo UiText'e geçir.
//                        _effect.send(HomeEffect.ShowMessage("Tüm sesler indirildi!"))
                    }
                    is BatchDownloadStatus.Error -> {
                        _state.update { it.copy(isDownloadingAll = false) }
                        _effect.send(HomeEffect.ShowMessage(status.message))
                    }
                }
            }
        }
    }

    private fun dismissBanner() {
        screenModelScope.launch {
            bannerUseCases.dismiss()
            _state.update { it.copy(showDownloadBanner = false) }
        }
    }
}