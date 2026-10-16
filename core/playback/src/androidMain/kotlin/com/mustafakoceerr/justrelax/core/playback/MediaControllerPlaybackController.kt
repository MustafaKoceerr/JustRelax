package com.mustafakoceerr.justrelax.core.playback

import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

/**
 * Talks to the playback service through a Media3 [MediaController], the way Media3 expects an app's
 * UI to. Connecting binds (and so starts) the service; the connection is kept while a screen
 * observes [isPlaying], plus a few seconds, and a command sent while disconnected waits for it.
 */
@OptIn(UnstableApi::class, ExperimentalCoroutinesApi::class)
class MediaControllerPlaybackController(
    context: Context,
    serviceComponent: ComponentName,
) : PlaybackController {

    private val scope = MainScope()

    private val controller: SharedFlow<MediaController> = callbackFlow {
        val future = MediaController.Builder(context, SessionToken(context, serviceComponent)).buildAsync()
        send(future.await())
        awaitClose { MediaController.releaseFuture(future) }
    }.shareIn(scope, SharingStarted.WhileSubscribed(CONNECTION_GRACE_MS), replay = 1)

    override val isPlaying: StateFlow<Boolean> = controller
        .flatMapLatest { controller ->
            callbackFlow {
                val listener = object : Player.Listener {
                    override fun onEvents(player: Player, events: Player.Events) {
                        trySend(!Util.shouldShowPlayButton(player))
                    }
                }
                trySend(!Util.shouldShowPlayButton(controller))
                controller.addListener(listener)
                awaitClose { controller.removeListener(listener) }
            }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(CONNECTION_GRACE_MS), false)

    override fun play() {
        scope.launch { Util.handlePlayButtonAction(controller.first()) }
    }

    override fun pause() {
        scope.launch { Util.handlePauseButtonAction(controller.first()) }
    }

    private companion object {
        const val CONNECTION_GRACE_MS = 5_000L
    }
}
