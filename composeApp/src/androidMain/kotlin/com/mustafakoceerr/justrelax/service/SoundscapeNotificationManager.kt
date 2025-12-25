package com.mustafakoceerr.justrelax.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.mustafakoceerr.justrelax.MainActivity
import com.mustafakoceerr.justrelax.R

class SoundscapeNotificationManager(
    private val context: Context,
    private val sessionToken: MediaSessionCompat.Token
) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    fun getNotification(isPlaying: Boolean): Notification {

        // 1. Play Action (Direkt Servise)
        val playIntent = Intent(context, SoundscapeService::class.java).apply {
            action = SoundscapeService.ACTION_PLAY
        }
        val playPendingIntent = PendingIntent.getService(
            context, 0, playIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val playAction = NotificationCompat.Action(
            android.R.drawable.ic_media_play, "Play", playPendingIntent
        )

        // 2. Pause Action (Direkt Servise)
        val pauseIntent = Intent(context, SoundscapeService::class.java).apply {
            action = SoundscapeService.ACTION_PAUSE
        }
        val pausePendingIntent = PendingIntent.getService(
            context, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val pauseAction = NotificationCompat.Action(
            android.R.drawable.ic_media_pause, "Pause", pausePendingIntent
        )

        // 3. Stop Action (Direkt Servise - Delete Intent için)
        val stopIntent = Intent(context, SoundscapeService::class.java).apply {
            action = SoundscapeService.ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            context, 2, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 4. Content Intent
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context, 3, contentIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(if (isPlaying) "Relaxing sounds playing..." else "Paused")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(contentPendingIntent)
            .setDeleteIntent(stopPendingIntent) // Kaydırınca kapat
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setOngoing(isPlaying)

        // Duruma göre butonu ekle
        if (isPlaying) {
            builder.addAction(pauseAction)
        } else {
            builder.addAction(playAction)
        }

        return builder
            .setStyle(
                MediaStyle()
                    .setMediaSession(sessionToken)
                    .setShowActionsInCompactView(0) // Eklediğimiz tek butonu göster
            )
            .build()
    }

    private fun createNotificationChannel() {
        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Soundscape Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls for Just Relax audio"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "just_relax_playback_channel"
        const val NOTIFICATION_ID = 888
    }
}