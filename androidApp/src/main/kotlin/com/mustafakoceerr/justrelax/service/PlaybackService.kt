package com.mustafakoceerr.justrelax.service

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.mustafakoceerr.justrelax.MainActivity
import com.mustafakoceerr.justrelax.R
import com.mustafakoceerr.justrelax.core.data.repository.SceneRepository
import com.mustafakoceerr.justrelax.core.playback.SoundscapePlayer
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

/**
 * Hosts the media session. The app's UI connects with a MediaController (which binds and starts
 * this service); Media3 owns the notification and the foreground state.
 */
@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {

    private val sceneRepository: SceneRepository by inject()
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this)
                .setChannelName(R.string.notification_channel_name)
                .build()
                .apply { setSmallIcon(R.drawable.ic_notification_small) }
        )

        val player: SoundscapePlayer = get { parametersOf(mixMetadata()) }
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                // Media3 started this service when playback began; an empty scene leaves nothing to
                // play. While the app is bound, this only ends the started state.
                if (playbackState == Player.STATE_IDLE) stopSelf()
            }
        })
        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(openAppIntent())
            .setMediaButtonPreferences(listOf(stopButton()))
            .setCallback(SessionCallback())
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }

    private fun mixMetadata() = MediaMetadata.Builder()
        .setTitle(getString(R.string.notification_title))
        .setArtist(getString(R.string.notification_subtitle))
        .setArtworkUri(
            // Built from the resource id (not its name) so the resource shrinker sees the reference.
            Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(packageName)
                .appendPath(R.drawable.notification_artwork.toString())
                .build()
        )
        .build()

    private fun stopButton() = CommandButton.Builder(CommandButton.ICON_STOP)
        .setDisplayName(getString(R.string.action_stop))
        .setSessionCommand(STOP_COMMAND)
        .build()

    /** Allows the custom Stop command for every controller (system UI, Bluetooth, Auto). */
    private inner class SessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult = MediaSession.ConnectionResult.AcceptedResultBuilder(session, controller)
            .setAvailableSessionCommands(
                MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon().add(STOP_COMMAND).build()
            )
            .build()

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> {
            if (customCommand.customAction != ACTION_STOP) {
                return super.onCustomCommand(session, controller, customCommand, args)
            }
            sceneRepository.clear()
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    private fun openAppIntent(): PendingIntent = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    companion object {
        /** Custom session command: Android 13+ system media controls only show custom actions. */
        const val ACTION_STOP = "com.mustafakoceerr.justrelax.STOP"
        private val STOP_COMMAND = SessionCommand(ACTION_STOP, Bundle.EMPTY)
    }
}
