package com.mustafakoceerr.justrelax.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
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

        // 1. Rengi ve Arka Planı XML Kaynaklarından Yükle
        val brandColor = ContextCompat.getColor(context, R.color.notification_brand_color)
        val artwork = BitmapFactory.decodeResource(context.resources, R.drawable.notification_artwork)

        // 2. Play/Pause Aksiyonunu String Kaynakları ile Oluştur
        val playPauseIcon = if (isPlaying) R.drawable.ic_notification_pause else R.drawable.ic_notification_play
        val playPauseTitle = if (isPlaying) context.getString(R.string.action_pause) else context.getString(R.string.action_play)
        val playPauseActionIntent = if (isPlaying) {
            PendingIntent.getService(context, 1, Intent(context, SoundscapeService::class.java).apply { action = SoundscapeService.ACTION_PAUSE }, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getService(context, 0, Intent(context, SoundscapeService::class.java).apply { action = SoundscapeService.ACTION_PLAY }, PendingIntent.FLAG_IMMUTABLE)
        }
        val playPauseAction = NotificationCompat.Action.Builder(playPauseIcon, playPauseTitle, playPauseActionIntent).build()

        // 3. Diğer Intent'ler
        val stopIntent = PendingIntent.getService(context, 2, Intent(context, SoundscapeService::class.java).apply { action = SoundscapeService.ACTION_STOP }, PendingIntent.FLAG_IMMUTABLE)
        val contentIntent = PendingIntent.getActivity(context, 3, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)

        // --- BİLDİRİMİ OLUŞTURMA ---
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setLargeIcon(artwork)
            // String Kaynaklarını Kullan
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_subtitle))
            // Renk Kaynağını Kullan
//            .setColor(brandColor)
            .setColorized(false)
            .setShowWhen(false)
            .setContentIntent(contentIntent)
            .setDeleteIntent(stopIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setOngoing(isPlaying)
            .addAction(playPauseAction)
            .setStyle(
                MediaStyle()
                    .setMediaSession(sessionToken)
                    .setShowActionsInCompactView(0)
            )

        return builder.build()
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