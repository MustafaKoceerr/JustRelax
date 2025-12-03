package com.mustafakoceerr.justrelax.core.sound.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mustafakoceerr.justrelax.core.generated.resources.Res
import com.mustafakoceerr.justrelax.core.sound.domain.model.Sound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.util.ArrayDeque
import com.mustafakoceerr.justrelax.core.R

class JustRelaxService : Service() {

    private val binder = LocalBinder()

    // Aktif Çalanlar
    private val activePlayers = mutableMapOf<String, ExoPlayer>()

    // Player Havuzu (Yedekler)
    private val playerPool = ArrayDeque<ExoPlayer>()
    private val POOL_LIMIT = 4

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Master Durum
    private var isMasterPlaying = false

    companion object {
        const val CHANNEL_ID = "just_relax_manual_channel"
        const val NOTIFICATION_ID = 999
        const val ACTION_PLAY = "action_play"
        const val ACTION_PAUSE = "action_pause"
        const val ACTION_STOP = "action_stop"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> resumeAll()
            ACTION_PAUSE -> pauseAll()
            ACTION_STOP -> stopAll() // Bildirimden çarpıya basılırsa veya swipe edilirse
            else -> {
                // Servis ilk başladığında eğer aktif ses varsa bildirimi göster
                if (activePlayers.isNotEmpty()) updateNotification()
            }
        }
        return START_STICKY
    }

    // --- HAVUZ YÖNETİMİ ---
    private fun acquirePlayer(): ExoPlayer {
        val player: ExoPlayer
        if (playerPool.isNotEmpty()) {
            player = playerPool.pop()
            player.clearMediaItems()
            player.seekTo(0)
        } else {
            player = ExoPlayer.Builder(this).build()
        }
        player.repeatMode = Player.REPEAT_MODE_ONE
        player.volume = 0.5f
        return player
    }

    private fun releasePlayerToPool(player: ExoPlayer) {
        player.stop()
        player.clearMediaItems()
        if (playerPool.size < POOL_LIMIT) {
            playerPool.push(player)
        } else {
            player.release()
        }
    }

    // --- SES İŞLEMLERİ (RÜTBE YÖNETİMİ BURADA) ---

    @OptIn(ExperimentalResourceApi::class)
    fun playSound(sound: Sound, volume: Float) {
        if (activePlayers.containsKey(sound.id)) return

        val player = acquirePlayer()
        player.volume = volume

        serviceScope.launch {
            val uriString = Res.getUri("files/${sound.audioFileName}")
            player.setMediaItem(MediaItem.fromUri(uriString.toUri()))
            player.prepare()
            player.play()

            activePlayers[sound.id] = player

            if (!isMasterPlaying) {
                isMasterPlaying = true
                activePlayers.values.forEach { it.play() }
            }

            // 1. RÜTBE ATLA (Promote): Ses çalmaya başladı, bildirimi göster.
            updateNotification()
        }
    }

    fun stopSound(soundId: String) {
        activePlayers.remove(soundId)?.let { player ->
            releasePlayerToPool(player)
        }

        if (activePlayers.isEmpty()) {
            // 2. RÜTBE DÜŞÜR (Demote): Hiç ses kalmadı.
            // Bildirimi kaldır ama servisi ÖLDÜRME (stopSelf yok).
            // Activity bağlı olduğu sürece servis arka planda "Er" olarak yaşar.
            demoteService()
            isMasterPlaying = false
        } else {
            // Hala ses var, bildirimi güncelle (Sayı azaldı)
            updateNotification()
        }
    }

    fun setVolume(soundId: String, volume: Float) {
        activePlayers[soundId]?.volume = volume
    }

    fun pauseAll() {
        isMasterPlaying = false
        activePlayers.values.forEach { it.pause() }
        updateNotification() // Bildirim kalsın (Pause modunda)
    }

    fun resumeAll() {
        isMasterPlaying = true
        activePlayers.values.forEach { it.play() }
        updateNotification() // Bildirim Play moduna geçsin
    }

    fun stopAll() {
        isMasterPlaying = false
        activePlayers.values.forEach { releasePlayerToPool(it) }
        activePlayers.clear()

        // Kullanıcı "Stop" dediği için burada rütbe düşürüp dükkanı kapatabiliriz
        demoteService()
        // İsteğe bağlı: stopAll çağrıldığında tamamen öldürmek istersen stopSelf() ekle.
        // Ama havuzda kalsın istiyorsan stopSelf() çağırma.
        // Bildirimden swipe edilince (ACTION_STOP) stopSelf çağırmak mantıklı olabilir.
    }

    // --- RÜTBE YÖNETİM FONKSİYONLARI ---

    private fun updateNotification() {
        // startForeground servisi Foreground yapar ve bildirimi gösterir/günceller
        startForeground(NOTIFICATION_ID, createNotification())
    }

    private fun demoteService() {
        // stopForeground servisi Background'a düşürür ve bildirimi siler.
        // STOP_FOREGROUND_REMOVE: Bildirimi de kaldır demektir.
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun createNotification(): Notification {
        val openIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingOpenIntent = PendingIntent.getActivity(
            this, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playIntent = Intent(this, JustRelaxService::class.java).apply { action = ACTION_PLAY }
        val pauseIntent = Intent(this, JustRelaxService::class.java).apply { action = ACTION_PAUSE }

        // Bu intent bildirim sağa kaydırılınca (Dismiss) çalışır
        val stopIntent = Intent(this, JustRelaxService::class.java).apply { action = ACTION_STOP }

        val pPlay = PendingIntent.getService(this, 1, playIntent, PendingIntent.FLAG_IMMUTABLE)
        val pPause = PendingIntent.getService(this, 2, pauseIntent, PendingIntent.FLAG_IMMUTABLE)
        val pDelete = PendingIntent.getService(this, 3, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Just Relax")
            .setContentText(if (activePlayers.isEmpty()) "Hazır" else "${activePlayers.size} Ses Çalıyor")
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.ic_launcher_foreground
                )
            )
            .setContentIntent(pendingOpenIntent)
            .setOngoing(isMasterPlaying)
            .setDeleteIntent(pDelete) // Swipe edilince ACTION_STOP çalışır
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setStyle(MediaStyle().setShowActionsInCompactView(0))

        if (isMasterPlaying) {
            builder.addAction(android.R.drawable.ic_media_pause, "Pause", pPause)
        } else {
            builder.addAction(android.R.drawable.ic_media_play, "Play", pPlay)
        }

        return builder.build()
    }

    // --- SWIPE (UYGULAMA KAPATMA) KORUMASI ---
    override fun onTaskRemoved(rootIntent: Intent?) {
        if (isMasterPlaying) {
            // Müzik çalıyorsa: Sadece duraklat, bildirim kalsın (Spotify Style)
            pauseAll()
        } else {
            // Müzik çalmıyorsa ve kullanıcı app'i kapattıysa:
            // Artık servisin yaşamasına gerek yok.
            demoteService() // Bildirimi sil
            stopSelf() // Servisi öldür (Havuz falan kalmasın, pil yemesin)
        }
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel =
                NotificationChannel(CHANNEL_ID, "Mixer Control", NotificationManager.IMPORTANCE_LOW)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        // Aktifleri temizle
        activePlayers.values.forEach { it.release() }
        // Havuzu temizle
        while (playerPool.isNotEmpty()) {
            playerPool.pop().release()
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    inner class LocalBinder : Binder() {
        fun getService(): JustRelaxService = this@JustRelaxService
    }
}