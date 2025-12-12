package com.mustafakoceerr.justrelax.core.audio

import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer {
    /**
     * Oynatma durumu (true = çalıyor, false = durmuş)
     */
    val isPlaying: StateFlow<Boolean>

    /**
     * Ses çalmaya başla
     * @param soundId: Çalınacak sesin ID'si
     * @param url: Dosya yolu (Local path)
     * @param volume: Ses seviyesi (0.0 - 1.0)
     */
    suspend fun play(soundId: String, url: String, volume: Float)

    /**
     * Sesi duraklat (Anlık işlem, suspend değil)
     */
    fun pause(soundId: String)

    /**
     * Duraklatılan sesi devam ettir
     */
    fun resume(soundId: String)

    /**
     * Sesi tamamen durdur
     */
    fun stop(soundId: String)

    /**
     * Ses seviyesini değiştir
     */
    fun setVolume(soundId: String, volume: Float)

    /**
     * Tüm kaynakları temizle (Service bağlantısını kes)
     */
    suspend fun releaseAll()

    // Master Player'ı (ve dolayısıyla Notification'ı) yönetmek için
    fun pauseAll()
    fun resumeAll()
}