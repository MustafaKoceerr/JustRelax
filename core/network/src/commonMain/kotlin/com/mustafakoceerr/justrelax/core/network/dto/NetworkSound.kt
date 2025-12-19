package com.mustafakoceerr.justrelax.core.network.dto

import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.SoundCategory
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
    val name: String,
    val category: String, // Sunucudan String olarak geldiğini varsayıyoruz
    val iconUrl: String,
    val audioUrl: String
)

