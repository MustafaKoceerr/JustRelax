package com.mustafakoceerr.justrelax.core.testing.fake

import com.mustafakoceerr.justrelax.core.domain.repository.legal.LegalRepository
import com.mustafakoceerr.justrelax.core.domain.system.LanguageController
import com.mustafakoceerr.justrelax.core.domain.system.LanguageStrategy
import com.mustafakoceerr.justrelax.core.domain.system.SystemLauncher
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Records what would have been opened instead of launching other apps. */
class FakeSystemLauncher : SystemLauncher {
    val openedUrls = mutableListOf<String>()
    var storePageOpened = false
    var languageSettingsOpened = false
    var feedbackEmailTo: String? = null

    override fun sendFeedbackEmail(to: String, subject: String, body: String) {
        feedbackEmailTo = to
    }

    override fun openStorePage(appId: String?) {
        storePageOpened = true
    }

    override fun openUrl(url: String) {
        openedUrls += url
    }

    override fun openAppLanguageSettings() {
        languageSettingsOpened = true
    }
}

class FakeLanguageController(
    override val strategy: LanguageStrategy = LanguageStrategy.IN_APP,
) : LanguageController {
    override val currentLanguage = MutableStateFlow(AppLanguage.SYSTEM)

    override suspend fun setLanguage(language: AppLanguage) {
        currentLanguage.value = language
    }

    override fun getCurrentLanguage(): AppLanguage = currentLanguage.value
}

class FakeLegalRepository : LegalRepository {
    override fun getPrivacyPolicyUrl(languageCode: String) = "https://legal.test/$languageCode/privacy"
    override fun getTermsAndConditionsUrl(languageCode: String) = "https://legal.test/$languageCode/terms"
}
