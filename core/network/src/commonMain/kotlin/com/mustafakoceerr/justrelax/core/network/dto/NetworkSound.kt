package com.mustafakoceerr.justrelax.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
Backend'den gelen JSON yapısı ile bizim uygulama içinde kullandığımız Sound modeli birebir aynı olmayabilir. Belki sunucudan category alanı String olarak geliyor, biz içeride Enum kullanıyoruz.
Bu ayrımı yönetmek ve API değişikliklerinden UI'ı korumak için DTO (Data Transfer Object) katmanı kullanmak en temiz yöntemdir.
 */
/**
 * Sorumluluk: Sadece sunucudan gelen 'Sound' JSON nesnesini temsil etmek.
 * 'internal' olması, bu sınıfın sadece :core:network modülü içinde
 * görünür olmasını sağlar. Dışarıya sızmaz.
 */


@Serializable
internal data class NetworkSound(
    val id: String,
    // JSON'daki "names" objesini Map olarak karşılıyoruz
    val names: Map<String, String>,
    val category: String,
    @SerialName("icon_url") val iconUrl: String,   // JSON'daki snake_case'i camelCase'e mapledik
    @SerialName("audio_url") val audioUrl: String, // JSON'daki snake_case'i camelCase'e mapledik
    val version: Int,
    @SerialName("is_initial") val isInitial: Boolean = false,
    @SerialName("size_bytes") val sizeBytes: Long = 0
)