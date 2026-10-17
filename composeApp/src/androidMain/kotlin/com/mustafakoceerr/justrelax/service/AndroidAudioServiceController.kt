package com.mustafakoceerr.justrelax.service

import android.content.Context
import android.content.Intent
import android.util.Log
import com.mustafakoceerr.justrelax.core.audio.AudioServiceController

class AndroidAudioServiceController(
    private val context: Context
) : AudioServiceController {

    /**
     * A plain start is enough: Media3 promotes the service to the foreground itself once
     * playback is running. Sounds are started from the UI, so the app is in the foreground here.
     */
    override fun start() {
        try {
            context.startService(Intent(context, PlaybackService::class.java))
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Could not start playback service", e)
        }
    }

    private companion object {
        const val TAG = "AudioServiceController"
    }
}
