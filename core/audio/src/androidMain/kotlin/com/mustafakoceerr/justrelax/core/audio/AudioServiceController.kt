package com.mustafakoceerr.justrelax.core.audio

/**
 * Interface to control the Android foreground service from the AudioMixer.
 * Decouples the audio logic from the Android Service component.
 */
interface AudioServiceController {
    fun start()
    fun stop()
}