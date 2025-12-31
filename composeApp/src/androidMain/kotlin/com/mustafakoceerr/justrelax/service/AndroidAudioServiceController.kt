package com.mustafakoceerr.justrelax.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.mustafakoceerr.justrelax.core.audio.AudioServiceController

/**
 * AudioServiceController arayüzünün Android'e özel implementasyonu.
 * SoundscapeService'i başlatmak ve durdurmak için Intent'leri kullanır.
 */
class AndroidAudioServiceController(
    private val context: Context
) : AudioServiceController {

    override fun start() {
        // Servisi ön planda (foreground) başlatmak için bu metodu kullanıyoruz.
        // Android 8 ve üzeri için bu zorunludur.
        val intent = Intent(context, SoundscapeService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    override fun stop() {
        // Servise "kendini durdur" komutunu gönderiyoruz.
        val intent = Intent(context, SoundscapeService::class.java).apply {
            action = SoundscapeService.ACTION_STOP
        }
        try {
            context.startService(intent)
        } catch (e: Exception) {
            // Servis zaten ölmüşse veya arka planda başlatma kısıtlaması varsa
            // burası patlayabilir ama stop işlemi olduğu için kritik değil.
            e.printStackTrace()
        }
    }

}
