package com.mustafakoceerr.justrelax.core.audio

/**
 * AudioMixer'ın, Android Service'i başlatma ve durdurma taleplerini
 * iletmek için kullanacağı sözleşme.
 *
 * Bu arayüz, core:audio modülünün Android Service sınıfına doğrudan
 * bağımlı olmasını engeller (Dependency Inversion Principle).
 */
interface AudioServiceController {
    /**
     * Arka plan ses servisini başlatır.
     */
    fun start()

    /**
     * Arka plan ses servisini durdurur.
     */
    fun stop()
}