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
    /** Usage analytics and crash reports are sent. */
    val isUsageDataEnabled: Boolean = false,
    /** The store offers tips, so the support row shows. */
    val canTip: Boolean = false,
    val userMessage: UiText? = null,
)
