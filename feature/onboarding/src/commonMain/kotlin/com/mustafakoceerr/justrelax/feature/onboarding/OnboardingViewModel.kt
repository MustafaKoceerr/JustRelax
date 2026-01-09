package com.mustafakoceerr.justrelax.feature.onboarding

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.usecase.appsetup.SetAppSetupFinishedUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.GetSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadAllSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadInitialSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.sync.SyncSoundsUseCase
import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.extensions.calculateTotalSizeInMb
import com.mustafakoceerr.justrelax.feature.onboarding.mvi.DownloadOption
import com.mustafakoceerr.justrelax.feature.onboarding.mvi.OnboardingEffect
import com.mustafakoceerr.justrelax.feature.onboarding.mvi.OnboardingIntent
import com.mustafakoceerr.justrelax.feature.onboarding.mvi.OnboardingScreenStatus
import com.mustafakoceerr.justrelax.feature.onboarding.mvi.OnboardingState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val syncSoundsUseCase: SyncSoundsUseCase,
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

    private fun checkDataAndLoadConfig() {
        screenModelScope.launch {
            _state.update { it.copy(status = OnboardingScreenStatus.LOADING_CONFIG) }

            val currentSounds = getSoundsUseCase().first()

            if (currentSounds.isNotEmpty()) {
                calculateOptions(currentSounds)
            } else {
                val result = syncSoundsUseCase()

                if (result is Resource.Success) {
                    val newSounds = getSoundsUseCase().first()
                    calculateOptions(newSounds)
                } else {
                    _state.update { it.copy(status = OnboardingScreenStatus.NO_INTERNET) }
                }
            }
        }
    }

    private fun calculateOptions(sounds: List<Sound>) {
        val initialSounds = sounds.filter { it.isInitial }
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
                    _effect.send(OnboardingEffect.ShowError(status.message))
                    _state.update { it.copy(status = OnboardingScreenStatus.CHOOSING) }
                }
                else -> {}
            }
        }.launchIn(screenModelScope)
    }

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