package com.mustafakoceerr.justrelax.core.ui.util

interface SystemLauncher {
    // E-posta uygulamasını açar (Cihaz bilgileriyle birlikte)
    fun sendFeedbackEmail(to: String, subject: String, body: String)

    // İlgili uygulama mağazasını açar
    fun openStorePage(appId: String? = null) // iOS için App ID gerekir, Android paket ismini kendi bulur.

    // Gizlilik politikası vb. için tarayıcı açar
    fun openUrl(url: String)
}