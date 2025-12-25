package com.mustafakoceerr.justrelax.core.domain.system

/*

Çünkü bunlar İş Mantığı (Business Logic) değil, Platform/Arayüz (UI) aksiyonudur.
Domain: "Veriyi kaydet", "Hesaplama yap" der. (Data odaklı)
UI/Common: "Email uygulamasını aç", "Dili değiştir", "Linke git" der. (Aksiyon odaklı)
 */
interface SystemLauncher {
    // E-posta uygulamasını açar (Cihaz bilgileriyle birlikte)
    fun sendFeedbackEmail(to: String, subject: String, body: String)

    // İlgili uygulama mağazasını açar
    fun openStorePage(appId: String? = null) // iOS için App ID gerekir, Android paket ismini kendi bulur.

    // Gizlilik politikası vb. için tarayıcı açar
    fun openUrl(url: String)

    fun openAppLanguageSettings()
}