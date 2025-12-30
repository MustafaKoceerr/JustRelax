package com.mustafakoceerr.justrelax.core.common

/**
 * Uygulama içindeki tüm hataların atası.
 *
 * BEST PRACTICE NOTLARI:
 * 1. Localization: Hata mesajları burada hardcoded (sabit metin) olarak tutulmaz.
 *    Buradaki 'message' alanı sadece geliştirici için (Logcat/Debug) anlamlı teknik detayları içermelidir.
 *    Kullanıcıya gösterilecek metinler UI katmanında bu sınıfların türüne (is Check) göre belirlenir.
 *
 * 2. Hierarchy: Sealed class hiyerarşisi sayesinde 'when' bloklarında tüm hataları kapsadığımızdan emin oluruz.
 */
sealed class AppError(
    override val message: String? = null,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    // --- Ağ Hataları ---
    sealed class Network(message: String? = null, cause: Throwable? = null) : AppError(message, cause) {
        class NoInternet : Network("No internet connection")
        class TimeOut : Network("Connection timed out")
        data class ServerError(val code: Int) : Network("Server error: $code")
        class SerializationError : Network("Data parsing error")
        data class Unknown(override val cause: Throwable?) : Network("Unknown network error", cause)
    }

    // --- Depolama Hataları ---
    sealed class Storage(message: String? = null, cause: Throwable? = null) : AppError(message, cause) {
        class DiskFull : Storage("Not enough disk space")
        class FileNotFound : Storage("File not found")
        class PermissionDenied : Storage("Permission denied")
    }

    // --- Veritabanı Hataları ---
    sealed class Database(message: String? = null, cause: Throwable? = null) : AppError(message, cause) {
        data class SaveFailed(val details: String?) : Database("Save failed: $details")
        data class ReadFailed(val details: String?) : Database("Read failed: $details")
        class ItemNotFound : Database("Item not found in DB")
    }

    // --- AI (Yapay Zeka) Hataları ---
    sealed class Ai(message: String? = null, cause: Throwable? = null) : AppError(message, cause) {
        class NoDownloadedSounds : Ai("No downloaded sounds available for AI")
        data class ApiError(val code: Int, val details: String) : Ai("AI API Error ($code): $details")
        class EmptyResponse : Ai("AI returned empty response")
        data class ParsingError(override val cause: Throwable?) : Ai("AI response parsing failed", cause)
    }

    // --- Player Hataları ---
    sealed class Player(message: String? = null, cause: Throwable? = null) : AppError(message, cause) {
        data class LimitExceeded(val limit: Int) : Player("Max sound limit ($limit) exceeded")
        data class InitializationError(val details: String) : Player("Player init failed: $details")
    }

    // --- Mix Kaydetme Hataları ---
    sealed class SaveMix(message: String? = null) : AppError(message) {
        class EmptyName : SaveMix("Mix name cannot be empty")
        class NameAlreadyExists : SaveMix("Mix name already exists")
        class NoSoundsPlaying : SaveMix("No sounds playing to save")
    }

    // --- Bilinmeyen / Beklenmeyen ---
    data class Unknown(override val cause: Throwable?) : AppError("Unexpected error", cause)
}