package com.mustafakoceerr.justrelax.feature.settings.mvi

import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.model.AppTheme

data class SettingsState(
    val currentTheme: AppTheme = AppTheme.SYSTEM,
    val currentLanguage: AppLanguage = AppLanguage.SYSTEM,
    val isLanguageSheetOpen: Boolean = false,
    val isDownloadingLibrary: Boolean = false,
    val downloadProgress: Float = 0f,
    val isLibraryComplete: Boolean = false
)

sealed interface SettingsIntent {
    data class ChangeTheme(val theme: AppTheme) : SettingsIntent
    data object OpenLanguageSelection : SettingsIntent
    data object CloseLanguageSelection : SettingsIntent
    data class ChangeLanguage(val language: AppLanguage) : SettingsIntent
    data object RateApp : SettingsIntent
    data object SendFeedback : SettingsIntent
    data object OpenPrivacyPolicy : SettingsIntent
    data object OpenTermsAndConditions : SettingsIntent
    data object DownloadAllLibrary : SettingsIntent
}

sealed interface SettingsEffect {
    data class ShowMessage(val message: String) : SettingsEffect
}