package com.mustafakoceerr.justrelax.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.domain.repository.SettingsRepository
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.model.AppTheme
import com.mustafakoceerr.justrelax.core.ui.localization.LanguageSwitcher
import com.mustafakoceerr.justrelax.core.ui.util.SystemLauncher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val languageSwitcher: LanguageSwitcher,
    private val systemLauncher: SystemLauncher
) : ViewModel() {

    // --- Data States (Repository'den gelenler) ---
    val currentTheme = settingsRepository.getTheme()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.SYSTEM)

    val currentLanguage = settingsRepository.getLanguage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.ENGLISH)

    // --- UI State
    private val _isLanguageSheetOpen = MutableStateFlow(false)
    val isLanguageSheetOpen = _isLanguageSheetOpen.asStateFlow()
    fun onLanguageTileClicked() {
        // MANTIK: Platform destekliyorsa Sheet aç, desteklemiyorsa Ayarlara git.
        if (languageSwitcher.supportsInAppSwitching) {
            _isLanguageSheetOpen.value = true
        } else {
            languageSwitcher.openSystemSettings()
        }
    }

    fun onLanguageSelected(language: AppLanguage) {
        viewModelScope.launch {
            // 1. Repository'ye kaydet (Kalıcılık için)
            settingsRepository.saveLanguage(language)

            // 2. Platform dilini değiştir (Android için)
            languageSwitcher.updateLanguage(language)

            // 3. Sheet'i kapat
            _isLanguageSheetOpen.value = false
        }
    }

    // --- Theme Logic ---
    fun updateTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.saveTheme(theme)
        }
    }

    fun dismissLanguageSheet() {
        _isLanguageSheetOpen.value = false
    }

    // --- External Actions (Destek, Puanlama, Gizlilik) ---
    // String'leri UI (Composable) çözümler, buraya saf metin gelir.
    fun sendFeedback(supportEmail: String, subject: String, body: String) {
        systemLauncher.sendFeedbackEmail(to = supportEmail, subject = subject, body = body)
    }

    fun rateApp() {
        systemLauncher.openStorePage()
    }

    fun openPrivacyPolicy(url: String) {
        systemLauncher.openUrl(url)
    }
}