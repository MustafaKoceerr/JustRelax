package com.mustafakoceerr.justrelax.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.domain.repository.settings.UserPreferencesRepository
import com.mustafakoceerr.justrelax.core.domain.repository.sound.SoundRepository
import com.mustafakoceerr.justrelax.core.domain.system.LanguageController
import com.mustafakoceerr.justrelax.core.domain.system.LanguageStrategy
import com.mustafakoceerr.justrelax.core.domain.system.SystemLauncher
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.GetLegalUrlUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.sound.download.DownloadAllSoundsUseCase
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.model.AppTheme
import com.mustafakoceerr.justrelax.core.model.DownloadStatus
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import justrelax.feature.settings.generated.resources.Res
import justrelax.feature.settings.generated.resources.msg_all_sounds_downloaded
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
    soundRepository: SoundRepository,
    private val downloadAllSoundsUseCase: DownloadAllSoundsUseCase,
    private val getLegalUrlUseCase: GetLegalUrlUseCase,
    private val systemLauncher: SystemLauncher,
    private val languageController: LanguageController,
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
        screenState,
    ) { theme, language, libraryComplete, screen ->
        SettingsUiState(
            theme = theme,
            language = language,
            isLanguageSheetOpen = screen.isLanguageSheetOpen,
            isDownloadingLibrary = screen.isDownloadingLibrary,
            downloadProgress = screen.downloadProgress,
            isLibraryComplete = libraryComplete,
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
        viewModelScope.launch { systemLauncher.openUrl(getLegalUrlUseCase.getPrivacyPolicy()) }
    }

    fun openTermsAndConditions() {
        viewModelScope.launch { systemLauncher.openUrl(getLegalUrlUseCase.getTermsAndConditions()) }
    }

    fun downloadLibrary() {
        if (screenState.value.isDownloadingLibrary || uiState.value.isLibraryComplete) return
        screenState.update { it.copy(isDownloadingLibrary = true, downloadProgress = 0f) }

        // viewModelScope cancels the download when the user leaves Settings.
        viewModelScope.launch {
            downloadAllSoundsUseCase().collect { status ->
                when (status) {
                    is DownloadStatus.Progress -> screenState.update { it.copy(downloadProgress = status.percentage) }
                    is DownloadStatus.Completed -> screenState.update {
                        it.copy(isDownloadingLibrary = false, userMessage = UiText.Resource(Res.string.msg_all_sounds_downloaded))
                    }
                    is DownloadStatus.Error -> screenState.update {
                        it.copy(isDownloadingLibrary = false, userMessage = UiText.DynamicString(status.message))
                    }
                    else -> Unit
                }
            }
        }
    }

    fun onMessageShown() = screenState.update { it.copy(userMessage = null) }

    private companion object {
        const val FEEDBACK_EMAIL = "kocerlabs@gmail.com"
        const val FEEDBACK_SUBJECT = "Just Relax Feedback"
    }
}
