package com.mustafakoceerr.justrelax.service


import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.mustafakoceerr.justrelax.core.audio.AudioServiceController

class AndroidServiceController(
    private val context: Context
) : AudioServiceController {

    private companion object {
        const val TAG = "AndroidServiceController"
    }

    override fun start() {
        Log.i(TAG, "start() called. Sending Intent to SoundscapeService.")
        val intent = Intent(context, SoundscapeService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    override fun stop() {
        Log.i(TAG, "stop() called. Sending ACTION_STOP to SoundscapeService.")
        val intent = Intent(context, SoundscapeService::class.java).apply {
            action = SoundscapeService.ACTION_STOP
        }
        ContextCompat.startForegroundService(context, intent)
    }
}