package com.mustafakoceerr.justrelax.core.audio

/**
 * Starts the platform playback service (Android: Media3 MediaSessionService) when sound starts.
 * The service stops itself once the mixer has no active sounds.
 */
fun interface AudioServiceController {
    fun start()
}
