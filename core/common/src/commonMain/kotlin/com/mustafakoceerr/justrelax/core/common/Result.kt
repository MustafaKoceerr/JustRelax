package com.mustafakoceerr.justrelax.core.common

import kotlinx.serialization.Serializable

/**
 * Domain ve Data katmanları arasındaki veri taşıyıcı.
 * Exception fırlatmak yerine bu yapıyı döndürerek akışı kontrol ederiz.
 */
sealed interface AppResult<out D, out E : AppError> {
    data class Success<out D>(val data: D) : AppResult<D, Nothing>
    data class Error<out E : AppError>(val error: E) : AppResult<Nothing, E>
}

// Tüm hataların atası (Marker Interface)
interface AppError

// Genel uygulama hataları
@Serializable
enum class DataError : AppError {
    NETWORK_TIMEOUT,
    NETWORK_NO_INTERNET,
    SERVER_ERROR,
    DATABASE_ERROR,
    UNKNOWN
}