package com.mustafakoceerr.justrelax.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Sound(
    val id: String,
    val name: String,         // UI'da gösterilecek ad (Key olabilir, Localization için)
    val categoryId: String,   // Enum yerine String ID tutuyoruz (DB uyumu ve esneklik için)
    val iconUrl: String,      // İnternet üzerindeki ikon adresi
    val remoteUrl: String,    // İnternet üzerindeki ses dosyası adresi
    val localPath: String? = null // Eğer indirilmişse cihazdaki yolu, yoksa null
) {
    // Computed Property: Veritabanında tutulmaz, anlık hesaplanır.
    val isDownloaded: Boolean
        get() = localPath != null
}