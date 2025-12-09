package com.mustafakoceerr.justrelax.core.audio.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.SilenceMediaSource
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import com.mustafakoceerr.justrelax.core.audio.SoundPlayer
import org.koin.android.ext.android.inject

class JustRelaxMediaService : MediaSessionService() {

    private val soundPlayer: SoundPlayer by inject()
    private var mediaSession: MediaSession? = null
    private lateinit var dummyPlayer: ExoPlayer
    private var contentPendingIntent: PendingIntent? = null

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "just_relax_playback_channel"
        const val SILENCE_DURATION_US = 24L * 60L * 60L * 1000L * 1000L
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        // 1. Kukla Player (Sessizlik Kaynağı ile)
        dummyPlayer = ExoPlayer.Builder(this).build()
        val silenceSource = SilenceMediaSource(SILENCE_DURATION_US)
        dummyPlayer.setMediaSource(silenceSource)
        dummyPlayer.prepare()
        dummyPlayer.repeatMode = Player.REPEAT_MODE_ONE

        // 2. KÖPRÜ VE BUTON FİLTRESİ
        val forwardingPlayer = object : ForwardingPlayer(dummyPlayer) {
            override fun play() {
                soundPlayer.resumeAll()
                super.play()
            }
            override fun pause() {
                soundPlayer.pauseAll()
                super.pause()
            }
            override fun stop() {
                soundPlayer.stopAll()
                super.stop()
                stopSelf()
            }

            // --- DÜZELTME 1: SADECE PLAY/PAUSE GÖSTER ---
            // Sisteme "Benim yeteneklerim bunlar, fazlasını gösterme" diyoruz.
            override fun getAvailableCommands(): Player.Commands {
                return Player.Commands.Builder()
                    .add(Player.COMMAND_PLAY_PAUSE)
                    .add(Player.COMMAND_STOP)
                    // COMMAND_SEEK_TO_PREVIOUS veya NEXT eklemediğimiz için butonlar gider.
                    .build()
            }
        }

        // 3. Tıklama Intent'i (App'i Açmak İçin)
        val openIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        contentPendingIntent = if (openIntent != null) {
            PendingIntent.getActivity(
                this,
                0,
                openIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else null

        // 4. Session
        mediaSession = MediaSession.Builder(this, forwardingPlayer)
            .setSessionActivity(contentPendingIntent ?: return)
            .build()

        createChannel()
    }

    @OptIn(UnstableApi::class)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (!dummyPlayer.isPlaying) {
            dummyPlayer.play()
        }

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

    @OptIn(UnstableApi::class)
    private fun createNotification(): Notification {
        val mediaStyle = MediaStyleNotificationHelper.MediaStyle(mediaSession!!)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play) // Kendi ikonunu koy: R.drawable.ic_notification
            .setContentTitle("Just Relax")
            .setContentText("Playing your mix")
            .setStyle(mediaStyle)
            // --- DÜZELTME 2: TIKLAMA AKSİYONU ---
            // MediaStyle bazen session activity'i yutar.
            // Buraya explicitly (açıkça) veriyoruz.
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(CHANNEL_ID, "Playback Controls", NotificationManager.IMPORTANCE_LOW)
                manager.createNotificationChannel(channel)
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        mediaSession?.player?.stop()
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}