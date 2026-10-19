package com.mustafakoceerr.justrelax.core.media

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerItemFailedToPlayToEndTimeNotification
import platform.AVFoundation.forwardPlaybackEndTime
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVFoundation.seekToTime
import platform.CoreMedia.CMTimeMake
import platform.CoreMedia.kCMTimeZero
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.darwin.NSObjectProtocol

/**
 * Pauses the sound scene. Players in the same app do not interrupt each other on iOS, so a
 * preview asks for it, as Android's audio focus does: the scene stays paused afterwards.
 */
fun interface ScenePauser {
    fun pauseScene()
}

/** Previews a range with AVPlayer, which streams a Freesound URL as easily as it plays a file. */
@OptIn(ExperimentalForeignApi::class)
internal class AvClipPreviewer(private val scenePauser: ScenePauser) : ClipPreviewer {

    private val playing = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = playing.asStateFlow()

    private val player = AVPlayer()
    private val observers = mutableListOf<NSObjectProtocol>()

    override fun play(path: String, range: TrimRange, loop: Boolean) {
        stop()
        removeObservers()
        // A URL (a Freesound preview) is played as it is; anything else is a file on the device.
        val url = if ("://" in path) NSURL.URLWithString(path) else NSURL.fileURLWithPath(path)
        val item = AVPlayerItem(uRL = url ?: return)
        val start = CMTimeMake(value = range.startMs, timescale = MS)
        item.forwardPlaybackEndTime = CMTimeMake(value = range.endMs, timescale = MS)
        observe(AVPlayerItemDidPlayToEndTimeNotification, item) {
            if (loop) {
                player.seekToTime(start, toleranceBefore = kCMTimeZero.readValue(), toleranceAfter = kCMTimeZero.readValue())
                player.play()
            } else {
                playing.value = false
            }
        }
        observe(AVPlayerItemFailedToPlayToEndTimeNotification, item) { playing.value = false }

        player.replaceCurrentItemWithPlayerItem(item)
        player.seekToTime(start, toleranceBefore = kCMTimeZero.readValue(), toleranceAfter = kCMTimeZero.readValue())
        scenePauser.pauseScene()
        player.play()
        playing.value = true
    }

    override fun stop() {
        player.pause()
        playing.value = false
    }

    override fun release() {
        stop()
        removeObservers()
        player.replaceCurrentItemWithPlayerItem(null)
    }

    private fun observe(name: String?, item: AVPlayerItem, onEvent: () -> Unit) {
        observers += NSNotificationCenter.defaultCenter.addObserverForName(
            name = name,
            `object` = item,
            queue = NSOperationQueue.mainQueue,
        ) { _ -> onEvent() }
    }

    private fun removeObservers() {
        observers.forEach { NSNotificationCenter.defaultCenter.removeObserver(it) }
        observers.clear()
    }

    private companion object {
        const val MS = 1_000
    }
}
