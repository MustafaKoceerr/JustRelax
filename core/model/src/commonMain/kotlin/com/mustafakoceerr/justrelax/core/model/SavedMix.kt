package com.mustafakoceerr.justrelax.core.model

import kotlinx.serialization.Serializable

@Serializable
data class SavedMix(
    val id: Long, // Veritabanı ID'si (Otomatik artan)
    val name: String,
    val dateEpoch: Long, // Oluşturulma tarihi (Unix Timestamp)
    val sounds: List<SavedSound> // Mix'in içindeki sesler ve seviyeleri
)