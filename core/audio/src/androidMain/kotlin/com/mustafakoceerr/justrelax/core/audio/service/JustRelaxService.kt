package com.mustafakoceerr.justrelax.core.audio.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle

class JustRelaxService : Service() {

    private val binder = LocalBinder()
    private var mediaSession: MediaSessionCompat? = null

    // Player'dan gelen "Play/Pause tıklandı" olayını dinleyecek callback
    var onMasterToggleAction: (() -> Unit)? = null
    var onStopAction: (() -> Unit)? = null

    companion object {
        const val CHANNEL_ID = "just_relax_channel"
        const val NOTIFICATION_ID = 1
    }

    inner class LocalBinder : Binder() {
        fun getService(): JustRelaxService = this@JustRelaxService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initializeSession()
    }

    private fun initializeSession() {
        mediaSession = MediaSessionCompat(this, "JustRelaxSession").apply {
            isActive = true
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() { onMasterToggleAction?.invoke() }
                override fun onPause() { onMasterToggleAction?.invoke() }
                override fun onStop() { onStopAction?.invoke() }
            })
        }
    }

    // AndroidAudioPlayer burayı çağırarak bildirimi güncelleyecek
    fun updateNotification(isPlaying: Boolean, contentText: String) {
        val session = mediaSession ?: return

        // 1. Session State Güncelle
        val state = if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        session.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_STOP)
                .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f)
                .build()
        )

        // 2. Notification Oluştur
        val icon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseAction = NotificationCompat.Action(
            icon, if (isPlaying) "Pause" else "Play",
            androidx.media.session.MediaButtonReceiver.buildMediaButtonPendingIntent(
                this, if (isPlaying) PlaybackStateCompat.ACTION_PAUSE else PlaybackStateCompat.ACTION_PLAY
            )
        )

        // Uygulamayı açmak için Intent (Paket ismini kendi ana activity'ne göre ayarla)
        val openAppIntent = packageManager.getLaunchIntentForPackage(packageName)
        val contentIntent = PendingIntent.getActivity(this, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play) // Kendi ikonunu koy: R.drawable.ic_stat_name
            .setContentTitle("Just Relax")
            .setContentText(contentText)
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(playPauseAction) // Tek tuş: Play/Pause
            .setStyle(
                MediaStyle()
                    .setMediaSession(session.sessionToken)
                    .setShowActionsInCompactView(0)
            )
            .setOngoing(isPlaying) // Çalıyorsa silinemez
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    fun stopForegroundService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        mediaSession?.release()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID, "Mixer Controls", NotificationManager.IMPORTANCE_LOW
                ).apply { description = "Control your mix" }
                manager.createNotificationChannel(channel)
            }
        }
    }
}