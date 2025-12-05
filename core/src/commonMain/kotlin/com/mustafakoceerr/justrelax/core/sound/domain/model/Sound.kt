package com.mustafakoceerr.justrelax.core.sound.domain.model

data class Sound(
    val id: String,
    val name: String,             // ARTIK STRING (Resource değil)
    val category: SoundCategory,
    val iconUrl: String,          // Coil ile çekeceğiz
    val audioUrl: String,         // Uzak sunucu linki
    val localPath: String?,       // İndiyse dosya yolu, inmediyse null
    val isDownloaded: Boolean = localPath != null
)