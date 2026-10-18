package com.mustafakoceerr.justrelax.core.playback

import android.content.Context
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.common.audio.AudioBecomingNoisyManager
import androidx.media3.common.audio.AudioFocusManager
import androidx.media3.common.util.Clock
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.WakeLockManager
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.mustafakoceerr.justrelax.core.data.repository.SceneRepository
import com.mustafakoceerr.justrelax.core.playback.SoundscapeSession.PlayWhenReadyReason
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * The app's one Media3 player. A mix is many sounds at once, which no ExoPlayer plays, so this is
 * a [SimpleBasePlayer] over [SoundscapeSession] that exposes the whole scene as one endless item.
 *
 * It takes audio focus, "becoming noisy" and the wake lock from the same Media3 classes ExoPlayer
 * uses, so interruptions behave like any Media3 app: a call pauses and resumes afterwards, another
 * app's music pauses for good, unplugging headphones pauses, short sounds duck the mix.
 */
@OptIn(UnstableApi::class)
class SoundscapePlayer internal constructor(
    context: Context,
    private val sceneRepository: SceneRepository,
    private val session: SoundscapeSession,
    private val mixMetadata: MediaMetadata,
) : SimpleBasePlayer(Looper.getMainLooper()) {

    private val scope = MainScope()

    private val audioFocusManager = AudioFocusManager(
        context,
        Looper.getMainLooper(),
        object : AudioFocusManager.PlayerControl {
            override fun setVolumeMultiplier(volumeMultiplier: Float) = session.setDuckVolume(volumeMultiplier)
            override fun executePlayerCommand(playerCommand: Int) =
                applyFocusCommand(session.state.value.playWhenReady, playerCommand)
        },
    ).apply { setAudioAttributes(MEDIA_AUDIO_ATTRIBUTES) }

    private val becomingNoisyManager = AudioBecomingNoisyManager(
        context,
        Looper.getMainLooper(),
        Looper.getMainLooper(),
        { session.setPlayWhenReady(false, PlayWhenReadyReason.BECOMING_NOISY) },
        Clock.DEFAULT,
    )

    // Event layers are silent between clips; the CPU must not sleep through their timers.
    private val wakeLockManager = WakeLockManager(context, Looper.getMainLooper(), Clock.DEFAULT)
        .apply { setEnabled(true) }

    private var lastFocusInput: Pair<Boolean, Int>? = null

    private val observeJob = scope.launch {
        session.state.collect { state ->
            val playbackState = playbackStateOf(state)
            // Like ExoPlayer: ask for focus whenever playWhenReady or the playback state changes.
            val focusInput = state.playWhenReady to playbackState
            if (focusInput != lastFocusInput) {
                lastFocusInput = focusInput
                applyFocusCommand(state.playWhenReady, audioFocusManager.updateAudioFocus(state.playWhenReady, playbackState))
            }
            becomingNoisyManager.setEnabled(state.playWhenReady && state.hasContent)
            wakeLockManager.setStayAwake(state.isRunning)
            invalidateState()
        }
    }

    override fun getState(): State {
        val state = session.state.value
        return State.Builder()
            .setAvailableCommands(AVAILABLE_COMMANDS)
            .setPlayWhenReady(state.playWhenReady, state.reason.toMedia3())
            .setPlaybackState(playbackStateOf(state))
            .setPlaybackSuppressionReason(
                if (state.isSuppressed) Player.PLAYBACK_SUPPRESSION_REASON_TRANSIENT_AUDIO_FOCUS_LOSS
                else Player.PLAYBACK_SUPPRESSION_REASON_NONE
            )
            .setPlaylist(if (state.hasContent) listOf(mixItem()) else emptyList())
            .build()
    }

    override fun handleSetPlayWhenReady(playWhenReady: Boolean): ListenableFuture<*> {
        session.setPlayWhenReady(playWhenReady)
        return Futures.immediateVoidFuture()
    }

    /** Nothing to load up front: the scene is prepared as soon as it changes. */
    override fun handlePrepare(): ListenableFuture<*> = Futures.immediateVoidFuture()

    override fun handleStop(): ListenableFuture<*> {
        sceneRepository.clear()
        return Futures.immediateVoidFuture()
    }

    override fun handleRelease(): ListenableFuture<*> {
        observeJob.cancel()
        scope.cancel()
        audioFocusManager.release()
        becomingNoisyManager.setEnabled(false)
        wakeLockManager.setStayAwake(false)
        wakeLockManager.setEnabled(false)
        session.release()
        return Futures.immediateVoidFuture()
    }

    private fun applyFocusCommand(playWhenReady: Boolean, playerCommand: Int) {
        when (playerCommand) {
            AudioFocusManager.PLAYER_COMMAND_DO_NOT_PLAY -> {
                session.setSuppressed(false)
                if (playWhenReady) session.setPlayWhenReady(false, PlayWhenReadyReason.AUDIO_FOCUS_LOSS)
            }
            AudioFocusManager.PLAYER_COMMAND_WAIT_FOR_CALLBACK -> session.setSuppressed(playWhenReady)
            AudioFocusManager.PLAYER_COMMAND_PLAY_WHEN_READY -> session.setSuppressed(false)
        }
    }

    private fun playbackStateOf(state: SoundscapeSession.State) =
        if (state.hasContent) Player.STATE_READY else Player.STATE_IDLE

    private fun mixItem() = MediaItemData.Builder(MIX_ID)
        .setMediaItem(MediaItem.Builder().setMediaId(MIX_ID).setMediaMetadata(mixMetadata).build())
        .setDurationUs(C.TIME_UNSET)
        .setIsSeekable(false)
        .build()

    private fun PlayWhenReadyReason.toMedia3() = when (this) {
        PlayWhenReadyReason.USER, PlayWhenReadyReason.SCENE_CLEARED -> Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST
        PlayWhenReadyReason.AUDIO_FOCUS_LOSS -> Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS
        PlayWhenReadyReason.BECOMING_NOISY -> Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_BECOMING_NOISY
        PlayWhenReadyReason.SLEEP_TIMER -> Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM
    }

    private companion object {
        const val MIX_ID = "current_scene"

        val AVAILABLE_COMMANDS: Player.Commands = Player.Commands.Builder()
            .addAll(
                Player.COMMAND_PLAY_PAUSE,
                Player.COMMAND_PREPARE,
                Player.COMMAND_STOP,
                Player.COMMAND_GET_CURRENT_MEDIA_ITEM,
                Player.COMMAND_GET_METADATA,
                Player.COMMAND_GET_TIMELINE,
                Player.COMMAND_RELEASE,
            )
            .build()
    }
}
