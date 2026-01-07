package com.mustafakoceerr.justrelax.core.domain.system

interface SystemLauncher {
    fun sendFeedbackEmail(to: String, subject: String, body: String)
    fun openStorePage(appId: String? = null)
    fun openUrl(url: String)
    fun openAppLanguageSettings()
}