package com.mustafakoceerr.justrelax.feature.settings

import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.model.AppTheme
import com.mustafakoceerr.justrelax.core.ui.util.UiText

data class SettingsUiState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val isLanguageSheetOpen: Boolean = false,
    val isDownloadingLibrary: Boolean = false,
    val downloadProgress: Float = 0f,
    val isLibraryComplete: Boolean = false,
    val userMessage: UiText? = null,
)
