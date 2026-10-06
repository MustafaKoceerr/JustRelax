package com.mustafakoceerr.justrelax.service

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import androidx.core.content.ContextCompat
import com.mustafakoceerr.justrelax.core.audio.focus.PlaybackInterruptionPolicy
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import android.os.Bundle
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.mustafakoceerr.justrelax.MainActivity
import com.mustafakoceerr.justrelax.R
import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Hosts the media session for the sound mixer. Media3 owns the notification and the
 * foreground state: it promotes the service while the mix plays and demotes it when paused.
 */
@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {

    private val audioMixer: AudioMixer by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var mediaSession: MediaSession? = null
    private lateinit var interruptionPolicy: PlaybackInterruptionPolicy

    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                interruptionPolicy.onBecomingNoisy()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this)
                .setChannelName(R.string.notification_channel_name)
                .build()
                .apply { setSmallIcon(R.drawable.ic_notification_small) }
        )

        val player = MixerSessionPlayer(audioMixer, serviceScope, mixMetadata())
        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(openAppIntent())
            .setMediaButtonPreferences(listOf(stopButton()))
            .setCallback(SessionCallback())
            .build()
            // Media3 only manages the notification/foreground state of sessions added to the
            // service. Normally that happens when a MediaController binds; we are started with
            // startService, so the session is added explicitly.
            .also(::addSession)

        startInterruptionHandling()
        stopWhenMixBecomesEmpty()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    /** Keeps playing after the app is swiped away; a paused or empty mix is cleaned up. */
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            serviceScope.launch { audioMixer.stopAll() }
            stopSelf()
        }
    }

    override fun onDestroy() {
        unregisterReceiver(becomingNoisyReceiver)
        interruptionPolicy.stop()
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        serviceScope.cancel()
        super.onDestroy()
    }

    /** Phone calls, other apps taking audio focus, and headphones being unplugged. */
    private fun startInterruptionHandling() {
        val audioFocus = AndroidAudioFocus(getSystemService(AudioManager::class.java)) { change ->
            when (change) {
                AudioManager.AUDIOFOCUS_LOSS -> interruptionPolicy.onFocusLost(transient = false)
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> interruptionPolicy.onFocusLost(transient = true)
                AudioManager.AUDIOFOCUS_GAIN -> interruptionPolicy.onFocusGained()
            }
        }
        interruptionPolicy = PlaybackInterruptionPolicy(audioMixer, audioFocus, serviceScope).apply { start() }

        ContextCompat.registerReceiver(
            this,
            becomingNoisyReceiver,
            IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    private fun stopWhenMixBecomesEmpty() {
        serviceScope.launch {
            audioMixer.state
                .map { it.activeSounds.isNotEmpty() }
                .distinctUntilChanged()
                .drop(1) // only react to changes, not to the state at service start
                .filter { hasSounds -> !hasSounds }
                .collect { stopSelf() }
        }
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
        ): MediaSession.ConnectionResult = MediaSession.ConnectionResult.AcceptedResultBuilder(session)
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
            serviceScope.launch { audioMixer.stopAll() }
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
