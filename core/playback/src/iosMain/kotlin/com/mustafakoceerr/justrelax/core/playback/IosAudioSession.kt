package com.mustafakoceerr.justrelax.core.playback

import com.mustafakoceerr.justrelax.core.data.repository.SceneRepository
import com.mustafakoceerr.justrelax.core.playback.player.AvAudioGraph
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionInterruptionNotification
import platform.AVFAudio.AVAudioSessionInterruptionOptionKey
import platform.AVFAudio.AVAudioSessionInterruptionOptionShouldResume
import platform.AVFAudio.AVAudioSessionInterruptionTypeBegan
import platform.AVFAudio.AVAudioSessionInterruptionTypeKey
import platform.AVFAudio.AVAudioSessionRouteChangeNotification
import platform.AVFAudio.AVAudioSessionRouteChangeReasonKey
import platform.AVFAudio.AVAudioSessionRouteChangeReasonOldDeviceUnavailable
import platform.AVFAudio.AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation
import platform.AVFAudio.setActive
import platform.Foundation.NSBundle
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNumber
import platform.Foundation.NSOperationQueue
import platform.MediaPlayer.MPMediaItemArtwork
import platform.MediaPlayer.MPNowPlayingInfoCenter
import platform.MediaPlayer.MPRemoteCommandCenter
import platform.MediaPlayer.MPRemoteCommandHandlerStatusSuccess
import platform.UIKit.UIImage

/**
 * The soundscape's place in iOS: the playback audio session (keeps playing in the background,
 * silences other apps), the system's interruption and route-change hints, and the lock screen /
 * Control Center controls. Each follows iOS's default behaviour, as Media3 does on Android.
 */
@OptIn(ExperimentalForeignApi::class)
internal class IosAudioSession(
    private val session: SoundscapeSession,
    private val controller: PlaybackController,
    private val sceneRepository: SceneRepository,
    private val graph: AvAudioGraph,
    scope: CoroutineScope,
) {
    private val audioSession = AVAudioSession.sharedInstance()

    private val nowPlayingTexts = NowPlayingTexts(
        title = localized("now_playing_title", "Calming Sounds"),
        subtitle = localized("now_playing_subtitle", "Enjoy the moment with Just Relax"),
    )

    /** The Android notification's artwork, from the app's asset catalog. */
    private val artwork: MPMediaItemArtwork? = UIImage.imageNamed(ARTWORK_ASSET)?.let { image ->
        MPMediaItemArtwork(boundsSize = image.size) { _ -> image }
    }

    init {
        // The playback category is set by AvAudioGraph, before its engine is built.
        observe(AVAudioSessionInterruptionNotification) { it.interruptionEvent()?.let(session::onAudioSessionEvent) }
        observe(AVAudioSessionRouteChangeNotification) { notification ->
            if (notification.number(AVAudioSessionRouteChangeReasonKey) == AVAudioSessionRouteChangeReasonOldDeviceUnavailable) {
                session.onAudioSessionEvent(AudioSessionEvent.OutputLost)
            }
        }
        registerRemoteCommands()

        scope.launch {
            session.state.map { it.isRunning to it.isSuppressed }.distinctUntilChanged().collect { (running, suppressed) ->
                when {
                    running -> audioSession.setActive(true, error = null)
                    // During an interruption the system has already taken the session.
                    !suppressed -> {
                        graph.pause()
                        audioSession.setActive(false, AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation, error = null)
                    }
                }
            }
        }
        scope.launch {
            combine(session.state.map { it.hasContent }, controller.isPlaying, ::Pair)
                .distinctUntilChanged()
                .collect { (hasContent, playing) -> showNowPlaying(hasContent, playing) }
        }
    }

    /** Play, pause and Stop, as on the Android notification; a soundscape has no tracks to skip or seek. */
    private fun registerRemoteCommands() {
        val commands = MPRemoteCommandCenter.sharedCommandCenter()
        commands.playCommand.addTargetWithHandler { controller.play(); MPRemoteCommandHandlerStatusSuccess }
        commands.pauseCommand.addTargetWithHandler { controller.pause(); MPRemoteCommandHandlerStatusSuccess }
        commands.togglePlayPauseCommand.addTargetWithHandler {
            if (controller.isPlaying.value) controller.pause() else controller.play()
            MPRemoteCommandHandlerStatusSuccess
        }
        commands.stopCommand.addTargetWithHandler { sceneRepository.clear(); MPRemoteCommandHandlerStatusSuccess }
        listOf(
            commands.nextTrackCommand,
            commands.previousTrackCommand,
            commands.skipForwardCommand,
            commands.skipBackwardCommand,
            commands.seekForwardCommand,
            commands.seekBackwardCommand,
            commands.changePlaybackPositionCommand,
        ).forEach { it.enabled = false }
    }

    private fun showNowPlaying(hasContent: Boolean, playing: Boolean) {
        MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = nowPlayingInfo(hasContent, playing, nowPlayingTexts, artwork)
    }

    private fun observe(name: String?, onNotification: (NSNotification) -> Unit) {
        NSNotificationCenter.defaultCenter.addObserverForName(name, `object` = audioSession, queue = NSOperationQueue.mainQueue) {
            it?.let(onNotification)
        }
    }

    private fun NSNotification.interruptionEvent(): AudioSessionEvent? = when (number(AVAudioSessionInterruptionTypeKey)) {
        null -> null
        AVAudioSessionInterruptionTypeBegan -> AudioSessionEvent.InterruptionBegan
        else -> {
            val options = number(AVAudioSessionInterruptionOptionKey) ?: 0uL
            AudioSessionEvent.InterruptionEnded(shouldResume = options and AVAudioSessionInterruptionOptionShouldResume != 0uL)
        }
    }

    private fun NSNotification.number(key: String?): ULong? = when (val value = userInfo?.get(key)) {
        is NSNumber -> value.unsignedLongValue
        is Number -> value.toLong().toULong()
        else -> null
    }

    /** From the app's Localizable.strings, in the language iOS picked for the app. */
    private fun localized(key: String, english: String): String =
        NSBundle.mainBundle.localizedStringForKey(key, value = english, table = null)

    private companion object {
        const val ARTWORK_ASSET = "NowPlayingArtwork"
    }
}
