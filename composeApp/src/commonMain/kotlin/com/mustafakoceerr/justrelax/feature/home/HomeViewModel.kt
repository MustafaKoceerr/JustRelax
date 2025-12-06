package com.mustafakoceerr.justrelax.feature.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.composeapp.generated.resources.Res
import com.mustafakoceerr.justrelax.composeapp.generated.resources.download_complete
import com.mustafakoceerr.justrelax.composeapp.generated.resources.suggestion_hidden
import com.mustafakoceerr.justrelax.core.settings.domain.repository.SettingsRepository
import com.mustafakoceerr.justrelax.core.sound.domain.model.BatchDownloadStatus
import com.mustafakoceerr.justrelax.core.sound.domain.model.Sound
import com.mustafakoceerr.justrelax.core.sound.domain.model.SoundCategory
import com.mustafakoceerr.justrelax.core.sound.domain.repository.SoundRepository
import com.mustafakoceerr.justrelax.feature.home.domain.usecase.DownloadAllMissingSoundsUseCase
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeEffect
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeIntent
import com.mustafakoceerr.justrelax.feature.home.mvi.HomeState
import com.mustafakoceerr.justrelax.utils.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Burada navigasyonu direkt yapmayarak Effect ile ui'a git emri vererek, viewModel'imizin voyager (navigasyon kütüphanesi)'nden bağımsız
 * olmasını sağlıyoruz.
 */
class HomeViewModel(
    private val repository: SoundRepository,
    private val settingsRepository: SettingsRepository,
    private val downloadAllMissingSoundsUseCase: DownloadAllMissingSoundsUseCase
) : ScreenModel {

    // _state'i private tutup dışarıya read-only açıyoruz.
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    // Effectleri iletmek için Channel kullanıyoruz (Hot Stream)
    // Channel.BUFFERED: Alıcı hazır olmasa bile eventi tutar.
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

            // --- YENİ HANDLERS ---
            is HomeIntent.DownloadAllMissing -> downloadAllMissing()
            is HomeIntent.DismissBanner -> dismissBanner()
            is HomeIntent.ClearMessage -> _state.update { it.copy(snackbarMessage = null) }
        }
    }
    @OptIn(ExperimentalTime::class)
    private fun dismissBanner() {
        screenModelScope.launch {
            settingsRepository.setLastDownloadPromptTime(Clock.System.now().toEpochMilliseconds())
            _state.update {
                it.copy(
                    showDownloadBanner = false,
                    // Snackbar mesajını UiText ile set ediyoruz
                    snackbarMessage = UiText.StringResource(Res.string.suggestion_hidden)
                )
            }
        }
    }
    private fun loadSounds() {
        screenModelScope.launch {
            repository.getSounds().collect { sounds ->
                allSoundsCache = sounds
                filterSoundsByCategory(_state.value.selectedCategory)

                // Sesler yüklendiğinde banner kontrolü yap
                checkMissingSounds(sounds)
            }
        }
    }
    // Banner Gösterme Mantığı (3 Gün Kuralı)
    @OptIn(ExperimentalTime::class)
    private fun checkMissingSounds(sounds: List<Sound>) {
        screenModelScope.launch {
            val missingCount = sounds.count { !it.isDownloaded }

            if (missingCount > 0) {
                val lastPrompt = settingsRepository.getLastDownloadPromptTime()
                val now = Clock.System.now().toEpochMilliseconds()
                val threeDaysInMillis = 3 * 24 * 60 * 60 * 1000L

                if (lastPrompt == 0L || (now - lastPrompt) > threeDaysInMillis) {
                    _state.update { it.copy(showDownloadBanner = true) }
                }
            } else {
                // Eksik yoksa banner'ı gizle
                _state.update { it.copy(showDownloadBanner = false) }
            }
        }
    }
    private fun selectCategory(category: SoundCategory) {
        _state.update { it.copy(selectedCategory = category) }
        filterSoundsByCategory(category)
    }

    private fun filterSoundsByCategory(category: SoundCategory) {
        val filtered = allSoundsCache.filter { it.category == category }
        _state.update { it.copy(sounds = filtered, isLoading = false) }
    }

    private fun onSettingsClicked() {
        // Navigasyon yapmıyoruz, "Git" emri veriyoruz.
        screenModelScope.launch {
            _effect.send(HomeEffect.NavigateToSettings)
        }
    }

    private fun downloadAllMissing() {
        screenModelScope.launch {
            _state.update { it.copy(isDownloadingAll = true, totalDownloadProgress = 0f) }

            downloadAllMissingSoundsUseCase().collect { status ->
                when (status) {
                    is BatchDownloadStatus.Progress -> {
                        _state.update { it.copy(totalDownloadProgress = status.percentage) }
                    }
                    is BatchDownloadStatus.Completed -> {
                        _state.update {
                            it.copy(
                                isDownloadingAll = false,
                                showDownloadBanner = false,
                                // Başarı mesajı (UiText)
                                snackbarMessage = UiText.StringResource(Res.string.download_complete)
                            )
                        }
                    }
                    is BatchDownloadStatus.Error -> {
                        _state.update { it.copy(isDownloadingAll = false) }
                    }
                }
            }
        }
    }
}// end of the class