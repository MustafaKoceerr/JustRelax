package com.mustafakoceerr.justrelax.feature.settings.mvi

import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.model.AppTheme

// ViewModel dışında, dosyanın en üstünde tanımlanması best practice'dir.
// 1. STATE: Ekranın o anki "fotoğrafı". Immutable (Değiştirilemez).
data class SettingsState(
    // Global Ayarlar (Domain'den gelir)
    val currentTheme: AppTheme = AppTheme.SYSTEM,
    val currentLanguage: AppLanguage = AppLanguage.SYSTEM,

    // UI Durumları (Geçici)
    val isLanguageSheetOpen: Boolean = false,

    // İndirme Durumları (İlerisi için yer tutucu)
    val isDownloadingLibrary: Boolean = false,
    val downloadProgress: Float = 0f,
    val isLibraryComplete: Boolean = false
)

// 2. INTENT: Kullanıcının yapmak istediği eylemler.
sealed interface SettingsIntent {
    // Tema
    data class ChangeTheme(val theme: AppTheme) : SettingsIntent

    // Dil
    data object OpenLanguageSelection : SettingsIntent
    data object CloseLanguageSelection : SettingsIntent
    data class ChangeLanguage(val language: AppLanguage) : SettingsIntent

    // Genel Aksiyonlar
    data object RateApp : SettingsIntent
    data object SendFeedback : SettingsIntent
    data object OpenPrivacyPolicy : SettingsIntent
    data object OpenTermsAndConditions : SettingsIntent

    // İndirme
    data object DownloadAllLibrary : SettingsIntent
}

// 3. EFFECT: Tek seferlik olaylar (Toast, Snackbar, Navigasyon).
sealed interface SettingsEffect {
    data class ShowMessage(val message: String) : SettingsEffect
}
