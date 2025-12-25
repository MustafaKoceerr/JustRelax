package com.mustafakoceerr.justrelax.feature.settings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.GetAppLanguageUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.GetAppThemeUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.SetAppLanguageUseCase
import com.mustafakoceerr.justrelax.core.domain.usecase.settings.SetAppThemeUseCase
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.model.AppTheme
import com.mustafakoceerr.justrelax.core.domain.system.LanguageSwitcher
import com.mustafakoceerr.justrelax.core.domain.system.SystemLauncher
import com.mustafakoceerr.justrelax.feature.settings.mvi.SettingsEffect
import com.mustafakoceerr.justrelax.feature.settings.mvi.SettingsIntent
import com.mustafakoceerr.justrelax.feature.settings.mvi.SettingsState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    // Domain UseCases (Business Logic)
    private val getAppThemeUseCase: GetAppThemeUseCase,
    private val setAppThemeUseCase: SetAppThemeUseCase,
    private val getAppLanguageUseCase: GetAppLanguageUseCase,
    private val setAppLanguageUseCase: SetAppLanguageUseCase,
    // Platform Helpers (Infrastructure)
    private val systemLauncher: SystemLauncher,
    private val languageSwitcher: LanguageSwitcher
) : ScreenModel {
    // Single Source of Truth
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _effect = Channel<SettingsEffect>(capacity = Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        // ViewModel oluşur oluşmaz Domain'den güncel verileri dinlemeye başla
        observeTheme()
        observeLanguage()
    }

    // MVI Entry Point
    fun processIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ChangeTheme -> changeTheme(intent.theme)

            is SettingsIntent.OpenLanguageSelection -> openLanguageSheet()
            is SettingsIntent.CloseLanguageSelection -> closeLanguageSheet()
            is SettingsIntent.ChangeLanguage -> changeLanguage(intent.language)

            is SettingsIntent.RateApp -> systemLauncher.openStorePage()
            is SettingsIntent.SendFeedback -> sendFeedback()
            is SettingsIntent.OpenPrivacyPolicy -> systemLauncher.openUrl("https://justrelax.app/privacy") // Sabit URL config'den gelebilir

            is SettingsIntent.DownloadAllLibrary -> {
                // TODO: DownloadAllMissingSoundsUseCase implemente edildiğinde burası dolacak.
                // Şimdilik boş bırakıyoruz (YAGNI prensibi: İhtiyacın olmayan kodu yazma).
            }
        }
    }

    private fun observeTheme() {
        screenModelScope.launch {
            getAppThemeUseCase().collectLatest { theme ->
                _state.update { it.copy(currentTheme = theme) }
            }
        }
    }

    private fun observeLanguage() {
        screenModelScope.launch {
            getAppLanguageUseCase().collectLatest { language ->
                _state.update { it.copy(currentLanguage = language) }
            }
        }
    }

    private fun changeTheme(theme: AppTheme) {
        screenModelScope.launch {
            setAppThemeUseCase(theme)
        }
    }

    private fun openLanguageSheet() {
        if (languageSwitcher.supportsInAppSwitching) {
            _state.update { it.copy(isLanguageSheetOpen = true) }
        } else {
            // iOS gibi desteklemeyen platformlarda direkt ayarlara yönlendir
            systemLauncher.openAppLanguageSettings()
        }
    }

    private fun closeLanguageSheet() {
        _state.update { it.copy(isLanguageSheetOpen = false) }
    }

    private fun changeLanguage(language: AppLanguage) {
        screenModelScope.launch {
            // 1. Kalıcı hafızaya (DataStore) kaydet
            setAppLanguageUseCase(language)

            // 2. Platform dilini güncelle (Android için recreate tetikler)
            languageSwitcher.updateLanguage(language)

            // 3. Sheet'i kapat
            closeLanguageSheet()
        }
    }

    private fun sendFeedback() {
        systemLauncher.sendFeedbackEmail(
            to = "support@justrelax.app",
            subject = "Just Relax Feedback",
            body = ""
        )
    }
}