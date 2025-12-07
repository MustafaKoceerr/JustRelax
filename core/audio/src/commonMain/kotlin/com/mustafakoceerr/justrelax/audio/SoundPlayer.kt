package com.mustafakoceerr.justrelax.audio

import com.mustafakoceerr.justrelax.core.model.Sound

interface SoundPlayer{

    /**
     * Bir sesi çalmaya başlar.
     * Best Practice: I/O işlemi içerdiği için "suspend" olmalıdır.
     * Hata yönetimi: Dosya bulunamazsa veya bozuksa exception fırlatabilir.
     * bunu ViewModel tarafında try-catch ile yakalayacağız.
     */
    suspend fun play(sound: Sound, volume: Float)

    /**
     * Belirli bir sesi durdurur ve kaynağını serbest bırakır.
     * Best practice: Sadece durdurmak yetmez, "release" edilmelidir.
     */
    fun stop(soundId: String)

    /**
     * Ses seviyesini değiştirir.
     * Best practice: Slider performansını etkilememesi için "suspend" olmamalıdır.
     * Native playerlarda volume property'si hafiftir.
     */
    fun setVolume(soundId: String, volume: Float)

    /**
     * Tüm sesleri durdurur
     */
    fun stopAll()

    /**
     * Kritik: Uygulama kapanırken veya viewModel ölürken
     * tüm native kaynakları (ExoPlayer/AVAudioPlayer) temizler.
     * Memory Leak'i önler.
     */
    fun release()


    fun pause(soundId: String)
    fun resume(soundId: String)
    fun pauseAll()
    fun resumeAll()

    // YENİ METOD: Toplu çalma işlemi (Mix için)
    // Sound ve Volume çiftlerini alır.
    suspend fun playMix(sounds: List<Pair<Sound, Float>>)
}
