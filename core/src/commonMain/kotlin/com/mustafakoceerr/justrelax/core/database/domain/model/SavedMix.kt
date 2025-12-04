package com.mustafakoceerr.justrelax.core.database.domain.model


data class SavedMix(
    val id: Long,
    val name: String,
    val dateEpoch: Long, // Tarihi Long (milisaniye) tutarız, UI istediği gibi formatlar (örn: "2 saat önce")
    val sounds: List<SavedSound> // İlişkisel veri (İçindeki sesler)
)


data class SavedSound(
    val id: String, // Sesin ID'si (örn: "rain")
    val volume: Float
)
