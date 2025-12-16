package com.mustafakoceerr.justrelax.core.ui.localization

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.ui.localization.LanguageSwitcher

class AndroidLanguageSwitcher : LanguageSwitcher {
    // Android uygulama içinde değiştirebilir -> TRUE
    override val supportsInAppSwitching: Boolean = true

    override suspend fun updateLanguage(language: AppLanguage) {
        val localeList = if (language == AppLanguage.SYSTEM){
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(language.code)
        }

        val currentLocales = AppCompatDelegate.getApplicationLocales()
        if (currentLocales.toLanguageTags() != localeList.toLanguageTags()) {
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }

    override fun openSystemSettings() {
        // Android'de kullanılmayacak ama interface gereği boş durabilir
    }
}