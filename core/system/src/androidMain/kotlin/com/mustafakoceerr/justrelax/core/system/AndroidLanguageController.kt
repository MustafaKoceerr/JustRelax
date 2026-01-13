package com.mustafakoceerr.justrelax.core.system

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.mustafakoceerr.justrelax.core.domain.system.LanguageController
import com.mustafakoceerr.justrelax.core.domain.system.LanguageStrategy
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

internal class AndroidLanguageController : LanguageController {

    override val strategy: LanguageStrategy = LanguageStrategy.IN_APP

    private val _currentLanguage = MutableStateFlow(getInitialLanguage())
    override val currentLanguage: Flow<AppLanguage> = _currentLanguage.asStateFlow()

    override suspend fun setLanguage(language: AppLanguage) {
        val localeList = if (language == AppLanguage.SYSTEM) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.create(Locale(language.code))
        }

        AppCompatDelegate.setApplicationLocales(localeList)

        _currentLanguage.value = language
    }

    private fun getInitialLanguage(): AppLanguage {
        val currentLocales = AppCompatDelegate.getApplicationLocales()

        return if (!currentLocales.isEmpty) {
            AppLanguage.fromCode(currentLocales.get(0)?.language ?: "en")
        } else {
            AppLanguage.fromCode(Locale.getDefault().language)
        }
    }

    override fun getCurrentLanguage(): AppLanguage {
        return _currentLanguage.value
    }

}