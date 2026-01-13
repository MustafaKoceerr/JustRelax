package com.mustafakoceerr.justrelax.core.system

import com.mustafakoceerr.justrelax.core.domain.system.LanguageController
import com.mustafakoceerr.justrelax.core.domain.system.LanguageStrategy
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

internal class IosLanguageController : LanguageController {

    override val strategy: LanguageStrategy = LanguageStrategy.SYSTEM_SETTINGS

    private val _currentLanguage = MutableStateFlow(getSystemLanguage())
    override val currentLanguage: Flow<AppLanguage> = _currentLanguage.asStateFlow()

    override suspend fun setLanguage(language: AppLanguage) {
        // "iOS does not support in-app switching"
    }
    private fun getSystemLanguage(): AppLanguage {
        val code = NSLocale.currentLocale.languageCode
        return AppLanguage.fromCode(code)
    }

    override fun getCurrentLanguage(): AppLanguage {
        return _currentLanguage.value
    }
}