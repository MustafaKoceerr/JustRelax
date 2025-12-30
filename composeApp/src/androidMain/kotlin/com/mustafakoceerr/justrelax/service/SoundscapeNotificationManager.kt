package com.mustafakoceerr.justrelax.service



import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat // <-- Ana Builder için doğru import
import androidx.media.app.NotificationCompat.MediaStyle // <-- Media Stili için doğru import
import com.mustafakoceerr.justrelax.MainActivity
import com.mustafakoceerr.justrelax.R

/**
 * Medya bildirimini oluşturur ve yönetir.
 */
class SoundscapeNotificationManager(
    private val context: Context,
    private val sessionToken: MediaSessionCompat.Token
) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    fun createNotification(isPlaying: Boolean): Notification {
        // Play/Pause butonu için dinamik intent oluşturma
        val playPauseIcon = if (isPlaying) R.drawable.ic_notification_pause else R.drawable.ic_notification_play
        val playPauseTitle = if (isPlaying) context.getString(R.string.action_pause) else context.getString(R.string.action_play)
        val playPauseAction = if (isPlaying) SoundscapeService.ACTION_PAUSE else SoundscapeService.ACTION_PLAY
        val playPauseIntent = PendingIntent.getService(
            context, 1,
            Intent(context, SoundscapeService::class.java).setAction(playPauseAction),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // Intent'in güncellenmesini sağlar
        )

        // Stop intent'i, sadece bildirimi kaydırarak kapatma (swipe) eylemi için var.
        val stopIntent = PendingIntent.getService(
            context, 2,
            Intent(context, SoundscapeService::class.java).setAction(SoundscapeService.ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )

        // Bildirime tıklandığında MainActivity'yi açacak intent
        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        // Bildirimi inşa et (Artık temiz görünüyor)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_subtitle))
            .setSmallIcon(R.drawable.ic_notification_small)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.notification_artwork))
            .setContentIntent(contentIntent)
            .setDeleteIntent(stopIntent) // Kullanıcı kaydırınca stop tetiklenir
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            // SADECE Play/Pause aksiyonunu ekliyoruz.
            .addAction(playPauseIcon, playPauseTitle, playPauseIntent)
            .setStyle(
                MediaStyle() // Temiz kullanım
                    .setMediaSession(sessionToken)
                    .setShowActionsInCompactView(0) // Sadece ilk (ve tek) aksiyon kompakt görünümde görünsün
            )
            .build()
    }

    private fun createNotificationChannel() {
        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Soundscape Controls",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls the soundscape from the notification shade."
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "just_relax_soundscape_channel"
    }
}