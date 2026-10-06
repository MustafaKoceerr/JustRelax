package com.mustafakoceerr.justrelax.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.repository.appsetup.AppSetupRepository
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadAllSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadInitialSoundsUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.sync.SyncSoundsUseCase
import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.extensions.calculateTotalSizeInMb
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val soundRepository: SoundRepository,
    private val appSetupRepository: AppSetupRepository,
    private val syncSoundsUseCase: SyncSoundsUseCase,
    private val downloadInitialSoundsUseCase: DownloadInitialSoundsUseCase,
    private val downloadAllSoundsUseCase: DownloadAllSoundsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        loadConfig()
    }

    fun retryLoadingConfig() = loadConfig()

    fun downloadStarterPack() = download(downloadInitialSoundsUseCase())

    fun downloadFullLibrary() = download(downloadAllSoundsUseCase())

    fun onMessageShown() = _uiState.update { it.copy(userMessage = null) }

    private fun loadConfig() {
        viewModelScope.launch {
            _uiState.update { it.copy(status = OnboardingStatus.LOADING_CONFIG) }

            val sounds = soundRepository.getSounds().first().ifEmpty {
                if (syncSoundsUseCase() !is Resource.Success) {
                    _uiState.update { it.copy(status = OnboardingStatus.NO_INTERNET) }
                    return@launch
                }
                soundRepository.getSounds().first()
            }
            showOptions(sounds)
        }
    }

    private fun showOptions(sounds: List<Sound>) {
        val starterSounds = sounds.filter { it.isInitial }
        _uiState.update {
            it.copy(
                status = OnboardingStatus.CHOOSING,
                starterPack = DownloadOption(starterSounds.calculateTotalSizeInMb(), starterSounds.size),
                fullLibrary = DownloadOption(sounds.calculateTotalSizeInMb(), sounds.size),
            )
        }
    }

    private fun download(downloads: Flow<DownloadStatus>) {
        if (_uiState.value.status == OnboardingStatus.DOWNLOADING) return
        _uiState.update { it.copy(status = OnboardingStatus.DOWNLOADING, downloadProgress = 0f) }

        // viewModelScope cancels the download if the user leaves onboarding.
        viewModelScope.launch {
            downloads.collect { status ->
                when (status) {
                    is DownloadStatus.Progress -> _uiState.update { it.copy(downloadProgress = status.percentage) }
                    is DownloadStatus.Completed -> {
                        appSetupRepository.setStarterPackInstalled(true)
                        _uiState.update { it.copy(status = OnboardingStatus.COMPLETED) }
                    }
                    is DownloadStatus.Error -> _uiState.update {
                        it.copy(status = OnboardingStatus.CHOOSING, userMessage = UiText.DynamicString(status.message))
                    }
                    else -> Unit
                }
            }
        }
    }
}
