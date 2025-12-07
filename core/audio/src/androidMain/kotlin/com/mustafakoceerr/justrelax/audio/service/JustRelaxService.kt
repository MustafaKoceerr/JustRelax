package com.mustafakoceerr.justrelax.audio.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.Service.START_STICKY
import android.app.Service.STOP_FOREGROUND_REMOVE
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mustafakoceerr.justrelax.core.model.Sound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.util.ArrayDeque

class JustRelaxService : Service() {

    private val binder = LocalBinder()

    // Aktif Çalanlar
    private val activePlayers = mutableMapOf<String, ExoPlayer>()

    // Player Havuzu (Yedekler)
    // Player Havuzu (Yedekler)
    private val playerPool = ArrayDeque<ExoPlayer>()
    private val POOL_LIMIT = 8
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

    fun playSound(sound: Sound, volume: Float) {
        if (activePlayers.containsKey(sound.id)) return

        // 1. Dosya Kontrolü (Safety Check)
        val path = sound.localPath
        if (path == null) {
            println("JustRelaxService: HATA - Ses indirilmemiş: ${sound.name}")
            return
        }

        val file = File(path)
        if (!file.exists()) {
            println("JustRelaxService: HATA - Dosya path var ama diskte yok: $path")
            return
        }

        val player = acquirePlayer()
        player.volume = volume

        serviceScope.launch {
            // 2. Dosyadan Oynatma (File -> Uri -> MediaItem)
            val uri = Uri.fromFile(file)
            player.setMediaItem(MediaItem.fromUri(uri))

            player.prepare()
            player.play()

            activePlayers[sound.id] = player

            if (!isMasterPlaying) {
                isMasterPlaying = true
                activePlayers.values.forEach { it.play() }
            }

            updateNotification()
        }
    }

    fun stopSound(soundId: String) {
        activePlayers.remove(soundId)?.let { player ->
            releasePlayerToPool(player)
        }

        if (activePlayers.isEmpty()) {
            demoteService()
            isMasterPlaying = false
        } else {
            updateNotification()
        }
    }

    fun setVolume(soundId: String, volume: Float) {
        activePlayers[soundId]?.volume = volume
    }

    fun pauseAll() {
        isMasterPlaying = false
        activePlayers.values.forEach { it.pause() }
        updateNotification()
    }

    fun resumeAll() {
        isMasterPlaying = true
        activePlayers.values.forEach { it.play() }
        updateNotification()
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
        startForeground(NOTIFICATION_ID, createNotification())
    }

    private fun demoteService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun createNotification(): Notification {
        val openIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingOpenIntent = PendingIntent.getActivity(
            this, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playIntent = Intent(this, JustRelaxService::class.java).apply { action = ACTION_PLAY }
        val pauseIntent = Intent(this, JustRelaxService::class.java).apply { action = ACTION_PAUSE }
        val stopIntent = Intent(this, JustRelaxService::class.java).apply { action = ACTION_STOP }

        val pPlay = PendingIntent.getService(this, 1, playIntent, PendingIntent.FLAG_IMMUTABLE)
        val pPause = PendingIntent.getService(this, 2, pauseIntent, PendingIntent.FLAG_IMMUTABLE)
        val pDelete = PendingIntent.getService(this, 3, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Just Relax")
            .setContentText(if (activePlayers.isEmpty()) "Hazır" else "${activePlayers.size} Ses Çalıyor")
            // Todo: buradaki icon'u değiştir.
            .setLargeIcon(BitmapFactory.decodeResource(resources, android.R.drawable.sym_def_app_icon))
            .setContentIntent(pendingOpenIntent)
            .setOngoing(isMasterPlaying)
            .setDeleteIntent(pDelete)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setStyle(MediaStyle().setShowActionsInCompactView(0))

        if (isMasterPlaying) {
            builder.addAction(android.R.drawable.ic_media_pause, "Pause", pPause)
        } else {
            builder.addAction(android.R.drawable.ic_media_play, "Play", pPlay)
        }

        return builder.build()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (isMasterPlaying) {
            pauseAll()
        } else {
            demoteService()
            stopSelf()
        }
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(CHANNEL_ID, "Mixer Control", NotificationManager.IMPORTANCE_LOW)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        activePlayers.values.forEach { it.release() }
        while (playerPool.isNotEmpty()) {
            playerPool.pop().release()
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    inner class LocalBinder : Binder() {
        fun getService(): JustRelaxService = this@JustRelaxService
    }

    fun playMix(sounds: List<Pair<Sound,Float>>){
        activePlayers.values.forEach { player ->
            player.pause()
            player.clearMediaItems()
            releasePlayerToPool(player)
        }
        activePlayers.clear()

        serviceScope.launch {
            sounds.forEach { (sound,volume)->
                // Dosya Kontrolü
                val path = sound.localPath
                if (path != null && File(path).exists()) {
                    val player = acquirePlayer()
                    player.volume = volume

                    // Dosyadan Uri oluştur
                    val uri = Uri.fromFile(File(path))
                    player.setMediaItem(MediaItem.fromUri(uri))

                    player.prepare()
                    player.play()
                    activePlayers[sound.id] = player
                } else {
                    println("JustRelaxService: Mix içindeki ses bulunamadı: ${sound.name}")
                }
            }
            isMasterPlaying = true
            updateNotification()
        }
    }
}