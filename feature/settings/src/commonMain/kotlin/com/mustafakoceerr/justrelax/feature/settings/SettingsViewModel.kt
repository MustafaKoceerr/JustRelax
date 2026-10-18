package com.mustafakoceerr.justrelax.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.data.repository.ConsentRepository
import com.mustafakoceerr.justrelax.core.data.repository.UserPreferencesRepository
import com.mustafakoceerr.justrelax.core.model.ConsentStatus
import com.mustafakoceerr.justrelax.core.data.repository.LegalRepository
import com.mustafakoceerr.justrelax.core.data.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.data.language.LanguageController
import com.mustafakoceerr.justrelax.core.data.language.LanguageStrategy
import com.mustafakoceerr.justrelax.core.ui.system.SystemLauncher
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.model.AppTheme
import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.msg_all_sounds_downloaded
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferences: UserPreferencesRepository,
    private val soundRepository: SoundRepository,
    private val legalRepository: LegalRepository,
    private val systemLauncher: SystemLauncher,
    private val languageController: LanguageController,
    private val consentRepository: ConsentRepository,
) : ViewModel() {

    /** State that only exists while this screen is open; everything else comes from the data layer. */
    private data class ScreenState(
        val isLanguageSheetOpen: Boolean = false,
        val isDownloadingLibrary: Boolean = false,
        val downloadProgress: Float = 0f,
        val userMessage: UiText? = null,
    )

    private val screenState = MutableStateFlow(ScreenState())

    private val isLibraryComplete = soundRepository.getSounds()
        .map { sounds -> sounds.isNotEmpty() && sounds.all { it.isDownloaded } }

    val uiState: StateFlow<SettingsUiState> = combine(
        preferences.getTheme(),
        preferences.getLanguage(),
        isLibraryComplete,
        consentRepository.consent,
        screenState,
    ) { theme, language, libraryComplete, consent, screen ->
        SettingsUiState(
            theme = theme,
            language = language,
            isLanguageSheetOpen = screen.isLanguageSheetOpen,
            isDownloadingLibrary = screen.isDownloadingLibrary,
            downloadProgress = screen.downloadProgress,
            isLibraryComplete = libraryComplete,
            isUsageDataEnabled = consent == ConsentStatus.GRANTED,
            userMessage = screen.userMessage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun changeTheme(theme: AppTheme) {
        viewModelScope.launch { preferences.setTheme(theme) }
    }

    fun openLanguageSelection() {
        when (languageController.strategy) {
            // Android: in-app language picker
            LanguageStrategy.IN_APP -> screenState.update { it.copy(isLanguageSheetOpen = true) }
            // iOS: the app language is changed in the system settings
            LanguageStrategy.SYSTEM_SETTINGS -> systemLauncher.openAppLanguageSettings()
        }
    }

    fun closeLanguageSelection() = screenState.update { it.copy(isLanguageSheetOpen = false) }

    fun changeLanguage(language: AppLanguage) {
        viewModelScope.launch {
            preferences.setLanguage(language)
            closeLanguageSelection()
        }
    }

    fun rateApp() = systemLauncher.openStorePage()

    fun sendFeedback() = systemLauncher.sendFeedbackEmail(
        to = FEEDBACK_EMAIL,
        subject = FEEDBACK_SUBJECT,
        body = "",
    )

    fun openPrivacyPolicy() {
        viewModelScope.launch { systemLauncher.openUrl(legalRepository.getPrivacyPolicyUrl()) }
    }

    fun openTermsAndConditions() {
        viewModelScope.launch { systemLauncher.openUrl(legalRepository.getTermsAndConditionsUrl()) }
    }

    fun downloadLibrary() {
        if (screenState.value.isDownloadingLibrary || uiState.value.isLibraryComplete) return
        screenState.update { it.copy(isDownloadingLibrary = true, downloadProgress = 0f) }

        // viewModelScope cancels the download when the user leaves Settings.
        viewModelScope.launch {
            soundRepository.downloadAll().collect { status ->
                when (status) {
                    is DownloadStatus.Progress -> screenState.update { it.copy(downloadProgress = status.percentage) }
                    is DownloadStatus.Completed -> screenState.update {
                        it.copy(isDownloadingLibrary = false, userMessage = UiText.Resource(Res.string.msg_all_sounds_downloaded))
                    }
                    is DownloadStatus.Error -> screenState.update {
                        it.copy(isDownloadingLibrary = false, userMessage = UiText.DynamicString(status.message))
                    }
                }
            }
        }
    }

    fun onMessageShown() = screenState.update { it.copy(userMessage = null) }

    private companion object {
        const val FEEDBACK_EMAIL = "kocerlabs@gmail.com"
        const val FEEDBACK_SUBJECT = "Just Relax Feedback"
    }

    /** Usage analytics and crash reports; the same answer as on first launch. */
    fun setUsageDataEnabled(enabled: Boolean) {
        viewModelScope.launch { consentRepository.setConsent(enabled) }
    }
}
