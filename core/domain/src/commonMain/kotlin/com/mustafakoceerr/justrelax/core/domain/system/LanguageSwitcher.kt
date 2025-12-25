package com.mustafakoceerr.justrelax.core.domain.system

import com.mustafakoceerr.justrelax.core.model.AppLanguage

/*

Çünkü bunlar İş Mantığı (Business Logic) değil, Platform/Arayüz (UI) aksiyonudur.
Domain: "Veriyi kaydet", "Hesaplama yap" der. (Data odaklı)
UI/Common: "Email uygulamasını aç", "Dili değiştir", "Linke git" der. (Aksiyon odaklı)
 */
interface LanguageSwitcher {
    // Platformun uygulama içi dil değişimini destekleyip desteklemediği
    // Android -> true (AppCompatDelegate ile)
    // iOS -> false (Genelde sistem ayarlarına yönlendirilir)
    val supportsInAppSwitching: Boolean

    // Dili değiştirir
    suspend fun updateLanguage(language: AppLanguage)

    // Mevcut sistem/uygulama dilini döndürür
    fun getSystemLanguage(): AppLanguage
}