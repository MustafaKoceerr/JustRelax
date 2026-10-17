package com.mustafakoceerr.justrelax.service

import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.common.util.UnstableApi
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.mustafakoceerr.justrelax.core.audio.soundscape.SoundscapeEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.guava.future
import kotlinx.coroutines.launch

/**
 * Exposes the whole [SoundscapeEngine] to Media3 as a single endless "mix" item, so the media
 * notification, lock screen and Bluetooth buttons can control it.
 *
 * The mixer stays the single source of truth: [getState] always reads its current state and
 * commands are forwarded to it. [scope] must run on the main thread.
 */
@OptIn(UnstableApi::class)
internal class MixerSessionPlayer(
    private val soundscapeEngine: SoundscapeEngine,
    private val scope: CoroutineScope,
    private val mixMetadata: MediaMetadata,
) : SimpleBasePlayer(Looper.getMainLooper()) {

    private val observeJob = scope.launch {
        soundscapeEngine.state.collect { invalidateState() }
    }

    override fun getState(): State {
        val mixerState = soundscapeEngine.state.value
        val hasSounds = mixerState.layers.isNotEmpty()

        return State.Builder()
            .setAvailableCommands(AVAILABLE_COMMANDS)
            .setPlayWhenReady(mixerState.isPlaying, Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST)
            .setPlaybackState(if (hasSounds) Player.STATE_READY else Player.STATE_IDLE)
            .setPlaylist(if (hasSounds) listOf(mixItem()) else emptyList())
            .build()
    }

    override fun handleSetPlayWhenReady(playWhenReady: Boolean): ListenableFuture<*> = scope.future {
        if (playWhenReady) soundscapeEngine.resumeAll() else soundscapeEngine.pauseAll()
    }

    override fun handleStop(): ListenableFuture<*> = scope.future {
        soundscapeEngine.stopAll()
    }

    override fun handleRelease(): ListenableFuture<*> {
        observeJob.cancel()
        return Futures.immediateVoidFuture()
    }

    private fun mixItem() = MediaItemData.Builder(MIX_ID)
        .setMediaItem(
            MediaItem.Builder()
                .setMediaId(MIX_ID)
                .setMediaMetadata(mixMetadata)
                .build()
        )
        .setDurationUs(C.TIME_UNSET)
        .setIsSeekable(false)
        .build()

    private companion object {
        const val MIX_ID = "current_mix"

        val AVAILABLE_COMMANDS: Player.Commands = Player.Commands.Builder()
            .addAll(
                Player.COMMAND_PLAY_PAUSE,
                Player.COMMAND_STOP,
                Player.COMMAND_GET_CURRENT_MEDIA_ITEM,
                Player.COMMAND_GET_METADATA,
                Player.COMMAND_GET_TIMELINE,
                Player.COMMAND_RELEASE,
            )
            .build()
    }
}
