package com.mustafakoceerr.justrelax.feature.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.audio.SoundManager
import com.mustafakoceerr.justrelax.core.audio.domain.usecase.ToggleSoundUseCase
import com.mustafakoceerr.justrelax.core.domain.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.model.BatchDownloadStatus
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeEffect
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeIntent
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeState
import com.mustafakoceerr.justrelax.feature.home.usecase.HomeBannerUseCases
import justrelax.feature.home.generated.resources.Res
import justrelax.feature.home.generated.resources.download_complete
import justrelax.feature.home.generated.resources.suggestion_hidden
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val soundRepository: SoundRepository,
    private val soundManager: SoundManager,
    private val toggleSoundUseCase: ToggleSoundUseCase,
    private val bannerUseCases: HomeBannerUseCases
) : ScreenModel {

    // İç state (Sadece Home verileri)
    private val _homeState = MutableStateFlow(HomeState())

    // İndirme durumları (Local State)
    private val _downloadingIds = MutableStateFlow<Set<String>>(emptySet())

    // DIŞARIYA AÇILAN STATE (Combine edilmiş)
    val state = combine(
        _homeState,
        soundManager.state,
        _downloadingIds
    ) { homeState, audioState, downloadingIds ->
        homeState.copy(
            activeSounds = audioState.activeSounds,
            downloadingSoundIds = downloadingIds
        )
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeState()
    )

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var allSoundsCache: List<Sound> = emptyList()

    init {
        processIntent(HomeIntent.LoadData)
    }

    fun processIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadData -> loadSounds()
            is HomeIntent.SelectCategory -> selectCategory(intent.category)
            is HomeIntent.SettingsClicked -> onSettingsClicked()
            is HomeIntent.DownloadAllMissing -> downloadAllMissing()
            is HomeIntent.DismissBanner -> dismissBanner()
            is HomeIntent.ClearMessage -> _homeState.update { it.copy(snackbarMessage = null) }

            // --- PLAYER INTENTLERİ ---
            is HomeIntent.ToggleSound -> toggleSound(intent.sound)
            is HomeIntent.ChangeVolume -> changeVolume(intent.id, intent.volume)
        }
    }

    private fun toggleSound(sound: Sound) {
        screenModelScope.launch {
            toggleSoundUseCase(sound, _downloadingIds.value.contains(sound.id))
                .collect { result ->
                    if (result is ToggleSoundUseCase.Result.Downloading) {
                        _downloadingIds.update {
                            if (result.isDownloading) it + sound.id else it - sound.id
                        }
                    }
                    // Diğer durumlar (Toggled) SoundManager tarafından yönetilir, state otomatik güncellenir.
                }
        }
    }

    private fun changeVolume(id: String, volume: Float) {
        soundManager.changeVolume(id, volume)
    }

    private fun dismissBanner() {
        screenModelScope.launch {
            bannerUseCases.dismiss()
            _homeState.update {
                it.copy(
                    showDownloadBanner = false,
                    snackbarMessage = UiText.StringResource(Res.string.suggestion_hidden)
                )
            }
        }
    }

    private fun loadSounds() {
        screenModelScope.launch {
            soundRepository.getSounds().collect { sounds ->
                allSoundsCache = sounds
                filterSoundsByCategory(_homeState.value.selectedCategory)
                checkMissingSounds(sounds)
            }
        }
    }

    private fun checkMissingSounds(sounds: List<Sound>) {
        screenModelScope.launch {
            val shouldShow = bannerUseCases.shouldShow(sounds)
            _homeState.update { it.copy(showDownloadBanner = shouldShow) }
        }
    }

    private fun selectCategory(category: SoundCategory) {
        _homeState.update { it.copy(selectedCategory = category) }
        filterSoundsByCategory(category)
    }

    private fun filterSoundsByCategory(category: SoundCategory) {
        val filtered = allSoundsCache.filter { it.category == category }
        _homeState.update { it.copy(sounds = filtered, isLoading = false) }
    }

    private fun onSettingsClicked() {
        screenModelScope.launch {
            _effect.send(HomeEffect.NavigateToSettings)
        }
    }

    private fun downloadAllMissing() {
        screenModelScope.launch {
            _homeState.update { it.copy(isDownloadingAll = true, totalDownloadProgress = 0f) }

            bannerUseCases.downloadAllMissingSounds().collect { status ->
                when (status) {
                    is BatchDownloadStatus.Progress -> {
                        _homeState.update { it.copy(totalDownloadProgress = status.percentage) }
                    }
                    is BatchDownloadStatus.Completed -> {
                        _homeState.update {
                            it.copy(
                                isDownloadingAll = false,
                                showDownloadBanner = false,
                                snackbarMessage = UiText.StringResource(Res.string.download_complete)
                            )
                        }
                    }
                    is BatchDownloadStatus.Error -> {
                        _homeState.update { it.copy(isDownloadingAll = false) }
                    }
                }
            }
        }
    }
}