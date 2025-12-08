package com.mustafakoceerr.justrelax.feature.settings


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakoceerr.justrelax.core.domain.repository.SettingsRepository
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.model.AppTheme
import com.mustafakoceerr.justrelax.core.ui.localization.LanguageSwitcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val languageSwitcher: LanguageSwitcher // Eklendi
) : ViewModel() {

    val currentTheme = settingsRepository.getTheme()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.SYSTEM)

    val currentLanguage = settingsRepository.getLanguage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.ENGLISH)

    fun updateTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.saveTheme(theme)
        }
    }

    fun updateLanguage(language: AppLanguage) {
        viewModelScope.launch {
            settingsRepository.saveLanguage(language)
            languageSwitcher.updateLanguage(language) // Platformu tetikle
        }
    }

    fun openSettings(){
        languageSwitcher.openSystemSettings()
    }
}