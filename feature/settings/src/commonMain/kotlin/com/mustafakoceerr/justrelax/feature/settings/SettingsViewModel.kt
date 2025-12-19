package com.mustafakoceerr.justrelax.feature.settings

import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.audio.domain.usecase.DownloadAllMissingSoundsUseCase
import com.mustafakoceerr.justrelax.core.ui.localization.LanguageSwitcher
import com.mustafakoceerr.justrelax.core.ui.util.SystemLauncher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val soundRepository: SoundRepository,
    private val downloadAllUseCase: DownloadAllMissingSoundsUseCase,
    private val languageSwitcher: LanguageSwitcher,
    private val systemLauncher: SystemLauncher
) : ScreenModel {

    // --- Data States (Kalıcı veriler) ---
    val currentTheme = settingsRepository.getTheme()
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.SYSTEM)

    val currentLanguage = settingsRepository.getLanguage()
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.ENGLISH)

    // --- Download States (Anlık durumlar) ---
    private val _isDownloading = MutableStateFlow(false)
    val isDownloading = _isDownloading.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress = _downloadProgress.asStateFlow()

    val isLibraryComplete = soundRepository.getSounds()
        .combine(_isDownloading) { sounds, isDownloading ->
            !isDownloading && sounds.isNotEmpty() && sounds.all { it.isDownloaded }
        }
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(5000), false)

    // --- UI States (Geçici durumlar) ---
    private val _isLanguageSheetOpen = MutableStateFlow(false)
    val isLanguageSheetOpen = _isLanguageSheetOpen.asStateFlow()

    // --- Effect Channel (Tek seferlik olaylar) ---
    private val _effect = Channel<SettingsEffect>()
    val effect = _effect.receiveAsFlow()


    // --- ACTIONS (UI'dan gelen eylemler) ---

    fun onDownloadAllClicked() {
        if (_isDownloading.value) return // Zaten iniyorsa tekrar başlatma

        // Todo: metinleri UiText'e taşı ayrıca global snackbar kullan.
        screenModelScope.launch {
//            _effect.send(SettingsEffect.ShowSnackbar("İndirme başlıyor..."))
            downloadAllUseCase().collect { status ->
                when (status) {
                    is BatchDownloadStatus.Progress -> {
                        _isDownloading.value = true
                        _downloadProgress.value = status.percentage
                    }
                    BatchDownloadStatus.Completed -> {
                        _isDownloading.value = false
                        _downloadProgress.value = 1f
//                        _effect.send(SettingsEffect.ShowSnackbar("Tüm sesler indirildi!"))
                    }
                    is BatchDownloadStatus.Error -> {
                        _isDownloading.value = false
//                        _effect.send(SettingsEffect.ShowSnackbar("Hata: İndirme başarısız oldu."))
                    }
                }
            }
        }
    }

    fun onLanguageTileClicked() {
        if (languageSwitcher.supportsInAppSwitching) {
            _isLanguageSheetOpen.value = true
        } else {
            languageSwitcher.openSystemSettings()
        }
    }

    fun onLanguageSelected(language: AppLanguage) {
        screenModelScope.launch {
            settingsRepository.saveLanguage(language)
            languageSwitcher.updateLanguage(language)
            dismissLanguageSheet()
        }
    }

    fun dismissLanguageSheet() {
        _isLanguageSheetOpen.value = false
    }

    fun updateTheme(theme: AppTheme) {
        screenModelScope.launch {
            settingsRepository.saveTheme(theme)
        }
    }

    fun sendFeedback(supportEmail: String, subject: String, body: String) {
        systemLauncher.sendFeedbackEmail(supportEmail, subject, body)
    }

    fun rateApp() {
        systemLauncher.openStorePage()
    }

    fun openPrivacyPolicy(url: String) {
        systemLauncher.openUrl(url)
    }
}