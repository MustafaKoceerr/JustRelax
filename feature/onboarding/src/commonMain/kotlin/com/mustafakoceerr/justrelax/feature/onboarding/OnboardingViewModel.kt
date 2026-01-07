package com.mustafakoceerr.justrelax.feature.onboarding

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.usecase.appsetup.SetAppSetupFinishedUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.GetSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.sync.SyncSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadAllSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadInitialSoundsUseCase
import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.extensions.calculateTotalSizeInMb
import com.mustafakoceerr.justrelax.feature.onboarding.mvi.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val syncSoundsUseCase: SyncSoundsUseCase, // Force Sync
    private val getSoundsUseCase: GetSoundsUseCase,
    private val downloadInitialSoundsUseCase: DownloadInitialSoundsUseCase,
    private val downloadAllSoundsUseCase: DownloadAllSoundsUseCase,
    private val setAppSetupFinishedUseCase: SetAppSetupFinishedUseCase
) : ScreenModel {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _effect = Channel<OnboardingEffect>()
    val effect = _effect.receiveAsFlow()

    private var downloadJob: Job? = null

    init {
        checkDataAndLoadConfig()
    }

    fun processIntent(intent: OnboardingIntent) {
        when (intent) {
            OnboardingIntent.RetryLoadingConfig -> checkDataAndLoadConfig()
            OnboardingIntent.DownloadInitial -> startDownload(downloadInitialSoundsUseCase())
            OnboardingIntent.DownloadAll -> startDownload(downloadAllSoundsUseCase())
        }
    }

    // 1. BAŞLANGIÇ KONTROLÜ (Init Logic)
    private fun checkDataAndLoadConfig() {
        screenModelScope.launch {
            _state.update { it.copy(status = OnboardingScreenStatus.LOADING_CONFIG) }

            // Adım A: Önce Veritabanına Bak (Hız için)
            val currentSounds = getSoundsUseCase().first()

            if (currentSounds.isNotEmpty()) {
                // Veri var! Direkt hesapla.
                calculateOptions(currentSounds)
            } else {
                // Adım B: Veri yok! Splash indirememiş veya internet yokmuş. Biz indiriyoruz.
                val result = syncSoundsUseCase()

                if (result is Resource.Success) {
                    // Başarılı, DB doldu, veriyi çek
                    val newSounds = getSoundsUseCase().first()
                    calculateOptions(newSounds)
                } else {
                    // HATA! İnternet yok.
                    _state.update { it.copy(status = OnboardingScreenStatus.NO_INTERNET) }
                }
            }
        }
    }

    // 2. SEÇENEKLERİ HESAPLAMA (Calculation)
    private fun calculateOptions(sounds: List<Sound>) {
        val initialSounds = sounds.filter { it.isInitial }

        // Extension fonksiyon ile hesapla
        val initialSize = initialSounds.calculateTotalSizeInMb()
        val totalSize = sounds.calculateTotalSizeInMb()

        _state.update {
            it.copy(
                status = OnboardingScreenStatus.CHOOSING,
                initialOption = DownloadOption(initialSize, initialSounds.size),
                allOption = DownloadOption(totalSize, sounds.size)
            )
        }
    }

    // 3. İNDİRME YÖNETİMİ (Downloading)
    private fun startDownload(downloadFlow: Flow<DownloadStatus>) {
        if (_state.value.status == OnboardingScreenStatus.DOWNLOADING) return

        _state.update { it.copy(status = OnboardingScreenStatus.DOWNLOADING, downloadProgress = 0f) }

        downloadJob = downloadFlow.onEach { status ->
            when (status) {
                is DownloadStatus.Progress -> {
                    _state.update { it.copy(downloadProgress = status.percentage) }
                }
                is DownloadStatus.Completed -> {
                    finishSetup()
                }
                is DownloadStatus.Error -> {
                    // Hata Yönetimi: Mesaj göster ve seçime geri dön
                    _effect.send(OnboardingEffect.ShowError(status.message))
                    _state.update { it.copy(status = OnboardingScreenStatus.CHOOSING) }
                }
                else -> {} // Queued
            }
        }.launchIn(screenModelScope)
    }

    // 4. KURULUMU TAMAMLAMA (Finalizing)
    private fun finishSetup() {
        screenModelScope.launch {
            setAppSetupFinishedUseCase()
            _state.update { it.copy(status = OnboardingScreenStatus.COMPLETED) }
            _effect.send(OnboardingEffect.NavigateToMainScreen)
        }
    }

    override fun onDispose() {
        downloadJob?.cancel()
        super.onDispose()
    }
}