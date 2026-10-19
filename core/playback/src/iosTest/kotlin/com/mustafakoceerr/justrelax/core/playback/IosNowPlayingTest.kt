package com.mustafakoceerr.justrelax.core.playback

import com.mustafakoceerr.justrelax.core.data.repository.InMemorySceneRepository
import com.mustafakoceerr.justrelax.core.data.repository.SceneRepository
import com.mustafakoceerr.justrelax.core.data.repository.SoundRepository
import com.mustafakoceerr.justrelax.core.playback.di.playbackModule
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.testing.testSound
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.Koin
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import platform.Foundation.NSData
import platform.Foundation.NSDataBase64DecodingIgnoreUnknownCharacters
import platform.Foundation.NSDate
import platform.Foundation.NSRunLoop
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.create
import platform.Foundation.dateWithTimeIntervalSinceNow
import platform.Foundation.runUntilDate
import platform.Foundation.writeToFile
import platform.MediaPlayer.MPMediaItemPropertyArtist
import platform.MediaPlayer.MPMediaItemPropertyTitle
import platform.MediaPlayer.MPNowPlayingInfoCenter
import platform.MediaPlayer.MPNowPlayingInfoPropertyPlaybackRate
import platform.MediaPlayer.MPRemoteCommandCenter
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The lock screen and Control Center through the real iOS graph. The simulator has no MediaRemote
 * service to draw them, so the test reads what the app hands to MPNowPlayingInfoCenter and
 * MPRemoteCommandCenter, which is all the system shows.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosNowPlayingTest {

    private val bed = NSTemporaryDirectory() + "now-playing-bed.ogg"
    private val sounds = FakeSoundRepository(listOf(testSound("rain", localPath = bed)))
    private val scene = InMemorySceneRepository(sounds)
    private lateinit var koin: Koin

    init {
        val data = NSData.create(base64EncodedString = BED_OGG_BASE64, options = NSDataBase64DecodingIgnoreUnknownCharacters)
        check(data?.writeToFile(bed, atomically = true) == true)
    }

    @AfterTest
    fun close() {
        runOnMainLoop { scene.clear() }
        koin.close()
    }

    private fun controller(): PlaybackController {
        koin = koinApplication {
            modules(
                module {
                    single<SoundRepository> { sounds }
                    single<SceneRepository> { scene }
                },
                playbackModule,
            )
        }.koin
        return koin.get()
    }

    private fun waitOnMainLoop(seconds: Double) {
        NSRunLoop.currentRunLoop.runUntilDate(NSDate.dateWithTimeIntervalSinceNow(seconds))
    }

    private fun runOnMainLoop(block: suspend () -> Unit) {
        val job = CoroutineScope(Dispatchers.Main).launch { block() }
        repeat(100) { if (!job.isCompleted) waitOnMainLoop(0.05) }
        check(job.isCompleted) { "timed out" }
        waitOnMainLoop(0.3)
    }

    private val nowPlaying get() = MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo

    @Test
    fun aPlayingMix_isOnTheLockScreen_withTheNotificationsWords() {
        val controller = controller()

        runOnMainLoop { scene.toggle("rain"); controller.play() }

        val info = checkNotNull(nowPlaying) { "nothing on the lock screen" }
        // The test bundle has no Localizable.strings, so the English words show.
        assertEquals("Calming Sounds", info[MPMediaItemPropertyTitle])
        assertEquals("Enjoy the moment with Just Relax", info[MPMediaItemPropertyArtist])
        assertEquals(1.0, info[MPNowPlayingInfoPropertyPlaybackRate])
    }

    @Test
    fun pausing_keepsTheEntryAtRateZero_andStoppingRemovesIt() {
        val controller = controller()
        runOnMainLoop { scene.toggle("rain"); controller.play() }

        runOnMainLoop { controller.pause() }
        assertEquals(0.0, nowPlaying?.get(MPNowPlayingInfoPropertyPlaybackRate))

        runOnMainLoop { scene.clear() }
        assertNull(nowPlaying)
    }

    @Test
    fun theControls_arePlayPauseAndStop_withNothingToSkipOrSeek() {
        controller()
        val commands = MPRemoteCommandCenter.sharedCommandCenter()

        assertTrue(commands.playCommand.enabled)
        assertTrue(commands.pauseCommand.enabled)
        assertTrue(commands.stopCommand.enabled)
        listOf(
            commands.nextTrackCommand,
            commands.previousTrackCommand,
            commands.skipForwardCommand,
            commands.skipBackwardCommand,
            commands.seekForwardCommand,
            commands.seekBackwardCommand,
            commands.changePlaybackPositionCommand,
        ).forEach { assertFalse(it.enabled, "a command to skip or seek is enabled") }
    }
}
