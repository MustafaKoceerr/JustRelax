package com.mustafakoceerr.justrelax.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import com.mustafakoceerr.justrelax.core.domain.player.GlobalMixerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Uygulamanın ses motorunu arka planda canlı tutan servis.
 * Bu servis "akılsızdır". Tek görevi AudioMixer'ın durumunu dinleyip
 * sisteme (Notification, Foreground State) yansıtmaktır.
 */
class SoundscapeService : Service() {

    private val audioMixer: AudioMixer by inject()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var notificationManager: SoundscapeNotificationManager
    private lateinit var mediaSessionManager: MediaSessionManager

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        // Yardımcı sınıfları oluştur ve birbirine bağla
        mediaSessionManager = MediaSessionManager(this, audioMixer, serviceScope)
        notificationManager =
            SoundscapeNotificationManager(this, mediaSessionManager.getSessionToken())

        // TEK GERÇEKLİK KAYNAĞINI DİNLE
        audioMixer.state
            .onEach { state ->
                handleStateChange(state)
            }
            .launchIn(serviceScope)
    }

    private fun handleStateChange(state: GlobalMixerState) {
        // 1. Medya oturumunu (kilit ekranı vs.) güncelle
        mediaSessionManager.updateState(state)

        // 2. Servisin ve bildirimin kaderini belirle
        if (state.activeSounds.isNotEmpty()) {
            // Bildirimi en güncel duruma göre oluştur
            val notification = notificationManager.createNotification(state.isPlaying)
            // Servisi ön plana taşı ve bildirimi göster/güncelle
            startForeground(NOTIFICATION_ID, notification)
        } else {
            // Çalacak ses kalmadıysa, servisi durdur
            stopService()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Bildirimden gelen komutları işle
        when (intent?.action) {
            ACTION_PAUSE -> serviceScope.launch { audioMixer.pauseAll() }
            ACTION_PLAY -> serviceScope.launch { audioMixer.resumeAll() }
            ACTION_STOP -> serviceScope.launch { audioMixer.stopAll() }
        }
        return START_NOT_STICKY
    }

    private fun stopService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // HOTFIX: Uygulama görev yöneticisinden atıldığında (Swipe),
        // AudioMixer Singleton olduğu için yaşamaya devam ediyor.
        // Servis ölmeden önce ses motorunu manuel olarak susturmamız lazım.
        serviceScope.launch {
            audioMixer.stopAll()
        }

        // Uygulama görev yöneticisinden kaydırıldığında servisi durdur.
        stopService()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        mediaSessionManager.release()
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 888
        const val ACTION_PLAY = "com.mustafakoceerr.justrelax.service.PLAY"
        const val ACTION_PAUSE = "com.mustafakoceerr.justrelax.service.PAUSE"
        const val ACTION_STOP = "com.mustafakoceerr.justrelax.service.STOP"
    }
}