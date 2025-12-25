package com.mustafakoceerr.justrelax.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Sound(
    val id: String,
    val names: Map<String, String>, // ARTIK TÜM DİLLERİ TUTUYORUZ
    val categoryId: String,   // Enum yerine String ID tutuyoruz (DB uyumu ve esneklik için)
    val iconUrl: String,      // İnternet üzerindeki ikon adresi
    val remoteUrl: String,    // İnternet üzerindeki ses dosyası adresi
    val localPath: String? = null, // Eğer indirilmişse cihazdaki yolu, yoksa null
    val isInitial: Boolean,
    val sizeBytes: Long
) {
    // Computed Property: Veritabanında tutulmaz, anlık hesaplanır.
    val isDownloaded: Boolean
        get() = localPath != null

    // UI bu fonksiyonu kullanacak.
    // Parametre olarak UI katmanındaki 'currentLanguage.code' verilecek.
    fun getDisplayName(languageCode: String): String {
        return names[languageCode]
            ?: names["en"]
            ?: names.values.firstOrNull()
            ?: id
    }
}