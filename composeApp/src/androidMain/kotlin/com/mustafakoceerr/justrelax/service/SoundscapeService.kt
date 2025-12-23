package com.mustafakoceerr.justrelax.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mustafakoceerr.justrelax.R // App Resource'larına erişim
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import org.koin.android.ext.android.inject

/**
 * Uygulamanın ses motorunu (AudioMixer) arka planda hayatta tutan Android Servisi.
 *
 * Görevleri:
 * 1. Foreground Service başlatarak Android'in uygulamayı öldürmesini engellemek.
 * 2. Notification göstermek.
 * 3. Uygulama kapatıldığında (Task kill) kaynakları temizlemek.
 */
class SoundscapeService : Service() {

    // Koin sayesinde :core:audio modülündeki implementation'ı alıyoruz.
    // Service, Mixer'ın "Nasıl" çalıştığını bilmez, sadece "Durdur/Temizle" der.
    private val audioMixer: AudioMixer by inject()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        // Servis başlar başlamaz Foreground moda geçmeli (Android 8+ kuralı)
        startForeground(NOTIFICATION_ID, createNotification())
    }

    // Bu servis "Started Service" tipindedir, "Bound Service" değil.
    // UI ile iletişimi UseCase'ler ve Singleton Mixer üzerinden olduğu için Bind etmeye gerek yok.
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Eğer servis bir şekilde ölürse ve sistem tekrar başlatırsa,
        // intent null gelebilir. Bu durumda tekrar başlatma (START_NOT_STICKY).
        // Çünkü ses çalmak kullanıcı isteğiyle olmalı, otomatik başlamamalı.

        when (intent?.action) {
            ACTION_STOP -> {
                audioMixer.stopAll()
                stopSelf() // Servisi ve Notification'ı öldür
            }
            // İleride buraya ACTION_PAUSE_ALL, ACTION_RESUME_ALL eklenebilir.
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        // Servis ölürken (Kullanıcı kapattı veya sistem öldürdü)
        // tüm sesleri durdur ve kaynakları serbest bırak.
        audioMixer.release()
        super.onDestroy()
    }

    // --- Notification Helper Methods ---

    private fun createNotification(): Notification {
        // Notification tıklandığında uygulamayı açmak için PendingIntent (Best Practice #1)
        // Not: Burada MainActivity::class.java'ya ihtiyacımız var, o yüzden app modülündeyiz.
        // val openAppIntent = Intent(this, MainActivity::class.java) ... (İleride eklenecek)

        // Stop Action (Notification üzerindeki Durdur butonu)
        // val stopIntent = Intent(this, SoundscapeService::class.java).apply { action = ACTION_STOP }
        // val stopPendingIntent = PendingIntent.getService(...)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Just Relax")
            .setContentText("Soundscape is playing...")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Geçici ikon
            .setOngoing(true) // Kullanıcı eliyle silemesin (ancak butonla)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            // .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent) // Buton eklenecek
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Soundscape Playback",
            NotificationManager.IMPORTANCE_LOW // Ses çıkarmasın, sadece görünsün
        ).apply {
            description = "Background audio playback controls"
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val NOTIFICATION_ID = 101
        const val CHANNEL_ID = "soundscape_channel"
        const val ACTION_STOP = "com.mustafakoceerr.justrelax.service.STOP"
    }
}