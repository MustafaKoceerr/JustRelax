package com.mustafakoceerr.justrelax.core.common

/**
* Uygulama içindeki tüm hataların atası.
* Exception'dan türetilmiştir ki gerektiğinde 'throw' edilebilsin.
*/
sealed class AppError(override val message: String, override val cause: Throwable? = null) : Exception() {

    // --- Ağ Hataları ---
    sealed class Network(message: String, cause: Throwable? = null) : AppError(message, cause) {
        class NoInternet : Network("İnternet bağlantısı yok.")
        class TimeOut : Network("Bağlantı zaman aşımına uğradı.")
        class ServerError(val code: Int) : Network("Sunucu hatası: $code")
        class SerializationError : Network("Veri işleme hatası.")
    }

    // --- Depolama Hataları ---
    sealed class Storage(message: String, cause: Throwable? = null) : AppError(message, cause) {
        class DiskFull : Storage("Yeterli depolama alanı yok.")
        class FileNotFound : Storage("Dosya bulunamadı.")
        class PermissionDenied : Storage("Dosya erişim izni yok.")
    }

    // --- Bilinmeyen / Beklenmeyen ---
    class Unknown(cause: Throwable?) : AppError("Beklenmedik bir hata oluştu.", cause)

    // --- Player Hataları ---
    sealed class Player(message: String, cause: Throwable? = null) : AppError(message, cause) {
        class LimitExceeded(val limit: Int) : Player("Aynı anda en fazla $limit ses çalınabilir.")
        class InitializationError(message: String) : Player(message)
    }

    // --- YENİ: Mix Kaydetme Hataları ---
    sealed class SaveMix(message: String) : AppError(message) {
        class EmptyName : SaveMix("Mix name cannot be empty.")
        class NameAlreadyExists : SaveMix("A mix with this name already exists.")
        class NoSoundsPlaying : SaveMix("There are no sounds currently playing to save.")
    }
}