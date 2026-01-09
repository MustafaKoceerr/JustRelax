package com.mustafakoceerr.justrelax.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.mustafakoceerr.justrelax.core.audio.AudioServiceController

class AndroidAudioServiceController(
    private val context: Context
) : AudioServiceController {

    override fun start() {
        val intent = Intent(context, SoundscapeService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    override fun stop() {
        val intent = Intent(context, SoundscapeService::class.java).apply {
            action = SoundscapeService.ACTION_STOP
        }
        try {
            context.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}