package com.mustafakoceerr.justrelax.core.common

/**
 * Uygulama içindeki tüm hataların atası.
 * Exception'dan türetilmiştir ki gerektiğinde 'throw' edilebilsin.
 */

sealed class AppError(override val message: String, override val cause: Throwable? = null) :
    Exception() {

    // --- Ağ Hataları ---
    sealed class Network(message: String, cause: Throwable? = null) : AppError(message, cause) {
        data object NoInternet : Network("İnternet bağlantısı yok.")
        data object TimeOut : Network("Bağlantı zaman aşımına uğradı.")
        data class ServerError(val code: Int) : Network("Sunucu hatası: $code")
        data object SerializationError : Network("Veri işleme hatası.")
    }

    // --- Depolama Hataları ---
    sealed class Storage(message: String, cause: Throwable? = null) : AppError(message, cause) {
        class DiskFull : Storage("Yeterli depolama alanı yok.")
        class FileNotFound : Storage("Dosya bulunamadı.")
        class PermissionDenied : Storage("Dosya erişim izni yok.")
    }

    // --- AI (Yapay Zeka) Hataları ---
    sealed class Ai(message: String, cause: Throwable? = null) : AppError(message, cause) {
        // Envanterde hiç ses yoksa AI çalışamaz
        data object NoDownloadedSounds : Ai("İndirilmiş ses bulunamadı. Lütfen önce ses indirin.")

        // API'den 200 harici bir kod geldiyse
        data class ApiError(val details: String) : Ai("AI Servisi yanıt vermedi: $details")

        // Cevap geldi ama içi boşsa
        data object EmptyResponse : Ai("AI boş bir cevap döndürdü.")

        // JSON parse edilemediyse
        data class ParsingError(override val cause: Throwable?) : Ai("AI cevabı işlenemedi.", cause)
    }

    // --- Player Hataları ---
    sealed class Player(message: String, cause: Throwable? = null) : AppError(message, cause) {
        data class LimitExceeded(val limit: Int) :
            Player("Aynı anda en fazla $limit ses çalınabilir.")

        data class InitializationError(val details: String) : Player(details)
    }

    // --- Mix Kaydetme Hataları ---
    sealed class SaveMix(message: String) : AppError(message) {
        data object EmptyName : SaveMix("Mix name cannot be empty.")
        data object NameAlreadyExists : SaveMix("A mix with this name already exists.")
        data object NoSoundsPlaying : SaveMix("There are no sounds currently playing to save.")
    }

    // --- Bilinmeyen / Beklenmeyen ---
    data class Unknown(override val cause: Throwable?) :
        AppError("Beklenmedik bir hata oluştu.", cause)
}