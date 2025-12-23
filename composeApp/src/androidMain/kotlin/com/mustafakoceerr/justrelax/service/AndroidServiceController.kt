package com.mustafakoceerr.justrelax.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.mustafakoceerr.justrelax.core.audio.AudioServiceController

class AndroidServiceController(
    private val context: Context
) : AudioServiceController {
    override fun start() {
        // Servisi "Foreground" olarak başlatma niyetimiz.
        // ContextCompat, Android sürümüne göre startForegroundService veya startService seçer.
        val intent = Intent(context, SoundscapeService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    override fun stop() {
        // Servisi durdurmak için özel bir Action gönderiyoruz.
        // Servis bu action'ı görünce kendini (stopSelf) kapatacak.
        val intent = Intent(context, SoundscapeService::class.java).apply {
            action = SoundscapeService.ACTION_STOP
        }
        ContextCompat.startForegroundService(context, intent)
    }
}