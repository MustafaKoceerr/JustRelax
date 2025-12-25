package com.mustafakoceerr.justrelax.core.domain.repository.savedmix

import com.mustafakoceerr.justrelax.core.model.Sound

/**
 * Veritabanından çekilen ve UI/Player tarafından kullanılacak olan Mix modeli.
 * Sound nesnesi saf olduğu için, ses seviyesini (Volume) burada eşleştiriyoruz.
 */
data class SavedMix(
    val id: Long,
    val name: String,
    val createdAt: String,
    // Anahtar: Sesin kendisi, Değer: Kaydedilen ses seviyesi (0.0 - 1.0)
    val sounds: Map<Sound, Float>
)