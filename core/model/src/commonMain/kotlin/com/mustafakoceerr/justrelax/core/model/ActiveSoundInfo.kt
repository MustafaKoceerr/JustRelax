package com.mustafakoceerr.justrelax.core.model

/**
 * Bir sesin o anki aktif durumunu temsil eden hafif veri sınıfı.
 * UseCase'ler arasında tam Sound objesini taşımak yerine kullanılır.
 */
data class ActiveSoundInfo(
    val id: String,
    val name: String,
    val volume: Float
)