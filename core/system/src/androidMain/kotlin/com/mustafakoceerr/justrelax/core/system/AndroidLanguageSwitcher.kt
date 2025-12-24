package com.mustafakoceerr.justrelax.core.system

import android.content.res.Resources
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.mustafakoceerr.justrelax.core.domain.system.LanguageSwitcher
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import java.util.Locale

class AndroidLanguageSwitcher : LanguageSwitcher {

    // Android 12 ve altı için 'true' döner, BottomSheet açılır.
    // Android 13+ için settings'e yönlendirilir.
    override val supportsInAppSwitching: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU


    override suspend fun updateLanguage(language: AppLanguage) {
        val localeList = if (language == AppLanguage.SYSTEM) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.create(Locale(language.code))
        }
        // Main Thread'de çalışması güvenlidir, activity'yi recreate eder.
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    override fun getSystemLanguage(): AppLanguage {
        // Cihazın o anki dilini alır
        val locale =
            Resources.getSystem().configuration.locales[0]
        return AppLanguage.fromCode(locale.language)
    }
}