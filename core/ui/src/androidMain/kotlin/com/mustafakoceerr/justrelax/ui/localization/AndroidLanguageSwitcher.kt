package com.mustafakoceerr.justrelax.ui.localization

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.mustafakoceerr.justrelax.core.model.AppLanguage

class AndroidLanguageSwitcher : LanguageSwitcher{

    override suspend fun updateLanguage(language: AppLanguage) {
        val localeList = if (language == AppLanguage.SYSTEM){
            LocaleListCompat.getEmptyLocaleList() // sistem varsayılanını uygula
        }else{
            LocaleListCompat.forLanguageTags(language.code) // seçili dili uygula
        }
        // Sonsuz döngü kontrolü: Zaten o dildeysek tekrar set etme!
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        if (currentLocales.toLanguageTags() != localeList.toLanguageTags()) {
            AppCompatDelegate.setApplicationLocales(localeList)
            // Best practices AppCompactDelegate ile dil değiştirmektir.
        }    }

    override fun openSystemSettings() {
    // androidde system settings açmıyoruz.
    }


}