package com.mustafakoceerr.justrelax.core.audio

/**
 * :core:audio modülü, SoundscapeService sınıfını tanımaz (Circular Dependency olmaması için).
 * Bu yüzden servisi başlatma işini bu arayüz üzerinden talep eder.
 *
 * DIP (Dependency Inversion): Mixer, Service'e değil, bu arayüze bağımlıdır.
 */
interface AudioServiceController {
    fun start()
    fun stop()
}