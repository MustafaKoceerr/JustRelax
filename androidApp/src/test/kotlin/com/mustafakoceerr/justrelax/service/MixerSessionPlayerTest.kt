package com.mustafakoceerr.justrelax.service

import android.app.Application
import android.os.Looper
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import com.mustafakoceerr.justrelax.core.playback.soundscape.SoundscapeState
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundscapeEngine
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
// Plain Application: the real one starts Koin, which this test does not need.
@Config(sdk = [35], application = Application::class)
class MixerSessionPlayerTest {

    private val mixer = FakeSoundscapeEngine()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val metadata = MediaMetadata.Builder().setTitle("Calming Sounds").build()
    private val player = MixerSessionPlayer(mixer, scope, metadata)

    @AfterTest
    fun tearDown() {
        player.release()
        scope.cancel()
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private fun mixerPlaying(isPlaying: Boolean = true) = mixer.setState(
        SoundscapeState(isPlaying = isPlaying, scene = Scene(listOf(Layer.Bed("rain", clip = "/rain.ogg", volume = 0.5f))))
    )

    @Test
    fun noActiveSounds_playerIsIdle() {
        idle()

        assertEquals(Player.STATE_IDLE, player.playbackState)
        assertEquals(0, player.mediaItemCount)
    }

    @Test
    fun activeSounds_playerIsReadyAndPlaying_withMixMetadata() {
        mixerPlaying()
        idle()

        assertEquals(Player.STATE_READY, player.playbackState)
        assertTrue(player.isPlaying)
        assertEquals("Calming Sounds", player.mediaMetadata.title)
    }

    @Test
    fun mixerPausedFromTheApp_isReflectedInPlayer() {
        mixerPlaying()
        idle()

        runBlocking { mixer.pauseAll() }
        idle()

        assertFalse(player.playWhenReady)
    }

    @Test
    fun pauseAndPlayFromNotification_controlTheMixer() {
        mixerPlaying()
        idle()

        player.pause()
        idle()
        assertFalse(mixer.state.value.isPlaying)
        assertFalse(player.playWhenReady)

        player.play()
        idle()
        assertTrue(mixer.state.value.isPlaying)
        assertTrue(player.playWhenReady)
    }

    @Test
    fun stopFromNotification_stopsAllSounds() {
        mixerPlaying()
        idle()

        player.stop()
        idle()

        assertTrue(mixer.state.value.layers.isEmpty())
        assertEquals(Player.STATE_IDLE, player.playbackState)
    }
}
