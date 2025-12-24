package com.mustafakoceerr.justrelax.service


import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject

class SoundscapeService : Service() {

    private val audioMixer: AudioMixer by inject()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var mediaSessionManager: MediaSessionManager
    private lateinit var notificationManager: SoundscapeNotificationManager
    private lateinit var keepAlivePlayer: KeepAlivePlayer
    private lateinit var mediaController: MediaControllerCompat

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        keepAlivePlayer = KeepAlivePlayer(this)
        mediaSessionManager = MediaSessionManager(this, audioMixer)
        notificationManager = SoundscapeNotificationManager(this, mediaSessionManager.getSessionToken())

        keepAlivePlayer.play()

        // 1. MediaSession Callback (Kilit ekranı vs. için)
        mediaController = MediaControllerCompat(this, mediaSessionManager.getSessionToken())
        mediaController.registerCallback(object : MediaControllerCompat.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                // Burası genelde sistemden gelen değişiklikler içindir,
                // ama asıl kaynağımız artık Mixer olacak.
            }
        })

        // 2. MIXER'I DİNLE: Sesler bitti mi?
        audioMixer.playingSoundIds
            .onEach { ids ->
                if (ids.isEmpty()) {
                    stopForegroundService()
                } else {
                    keepAlivePlayer.play()
                }
            }
            .launchIn(serviceScope)

        // --- EKLENEN KISIM BAŞLANGIÇ ---
        // 3. MIXER'I DİNLE: Play/Pause durumu değişti mi?
        // Uygulama içinden (BottomBar) basılınca burası tetiklenir.
        audioMixer.isPlaying
            .onEach { isPlaying ->
                // A. Bildirimi Güncelle
                updateNotification(isPlaying)

                // B. Kilit Ekranı / Sistem Durumunu Güncelle
                mediaSessionManager.updateState(isPlaying)

                // C. KeepAlive Yönetimi (Opsiyonel ama iyi olur)
                if (isPlaying) keepAlivePlayer.play() else keepAlivePlayer.pause()
            }
            .launchIn(serviceScope)
        // --- EKLENEN KISIM BİTİŞ ---
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                audioMixer.resumeAll()
                // Not: Buradan updateNotification çağırmaya gerek yok,
                // çünkü yukarıdaki 'audioMixer.isPlaying' flow'u tetiklenecek ve o yapacak.
            }
            ACTION_PAUSE -> {
                audioMixer.pauseAll()
            }
            ACTION_STOP -> {
                audioMixer.stopAll()
                stopForegroundService()
            }
        }

        // Servis ilk başladığında (henüz flow tetiklenmemiş olabilir) bildirimi göster
        if (intent?.action == null) {
            updateNotification(isPlaying = true)
        }

        return START_NOT_STICKY
    }

    private fun updateNotification(isPlaying: Boolean) {
        val notification = notificationManager.getNotification(isPlaying)
        // startForeground, servis çalışırken çağrılırsa sadece bildirimi günceller (titretmez).
        startForeground(SoundscapeNotificationManager.NOTIFICATION_ID, notification)
    }

    private fun stopForegroundService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        keepAlivePlayer.release()
        mediaSessionManager.release()
        audioMixer.stopAll()
        super.onDestroy()
    }

    companion object {
        const val ACTION_PLAY = "com.mustafakoceerr.justrelax.service.PLAY"
        const val ACTION_PAUSE = "com.mustafakoceerr.justrelax.service.PAUSE"
        const val ACTION_STOP = "com.mustafakoceerr.justrelax.service.STOP"
    }
}