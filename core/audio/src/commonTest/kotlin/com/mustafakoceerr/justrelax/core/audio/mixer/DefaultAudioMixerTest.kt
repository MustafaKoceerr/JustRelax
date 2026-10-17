package com.mustafakoceerr.justrelax.core.audio.mixer

import com.mustafakoceerr.justrelax.core.audio.AudioServiceController
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.AudioDefaults
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.domain.player.SoundConfig
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DefaultAudioMixerTest {

    private val factory = FakeSoundPlayerFactory()
    private var serviceStarts = 0
    private val serviceController = AudioServiceController { serviceStarts++ }

    private fun TestScope.mixer() = DefaultAudioMixer(factory, serviceController, backgroundScope)

    /** advanceUntilIdle() skips backgroundScope work, where the mixer launches fade-ins. */
    private fun TestScope.settle() {
        advanceUntilIdle()
        runCurrent()
    }

    private fun config(id: String, volume: Float = 0.5f) = SoundConfig(id, "/sounds/$id.mp3", volume, 0L)

    private fun DefaultAudioMixer.activeIds() = state.value.activeSounds.map { it.id }

    @Test
    fun playSound_startsServiceAndPlays() = runTest {
        val mixer = mixer()

        mixer.playSound(config("rain"))
        settle()

        assertEquals(1, serviceStarts)
        assertTrue(mixer.state.value.isPlaying)
        assertEquals(listOf("rain"), mixer.activeIds())
        assertTrue(factory.live().getValue("rain").isPlaying)
    }

    @Test
    fun playingSameSoundTwice_createsSinglePlayer() = runTest {
        val mixer = mixer()

        mixer.playSound(config("rain"))
        mixer.playSound(config("rain"))

        assertEquals(1, factory.live().size)
        assertEquals(listOf("rain"), mixer.activeIds())
    }

    @Test
    fun playingBeyondLimit_returnsLimitExceeded() = runTest {
        val mixer = mixer()
        repeat(AudioDefaults.MAX_CONCURRENT_SOUNDS) { mixer.playSound(config("s$it")) }

        val result = mixer.playSound(config("extra"))

        assertIs<AppError.Player.LimitExceeded>(assertIs<Resource.Error>(result).error)
        assertEquals(AudioDefaults.MAX_CONCURRENT_SOUNDS, factory.live().size)
    }

    @Test
    fun concurrentPlays_neverExceedTheLimit() = runTest {
        val mixer = mixer()
        factory.prepareDelayMs = 100

        repeat(AudioDefaults.MAX_CONCURRENT_SOUNDS + 3) { launch { mixer.playSound(config("s$it")) } }
        settle()

        assertEquals(AudioDefaults.MAX_CONCURRENT_SOUNDS, factory.live().size)
        assertEquals(AudioDefaults.MAX_CONCURRENT_SOUNDS, mixer.activeIds().size)
    }

    @Test
    fun failedPrepare_returnsError_andReleasesPlayer() = runTest {
        val mixer = mixer()
        factory.failingIds += "broken"

        val result = mixer.playSound(config("broken"))

        assertIs<Resource.Error>(result)
        assertTrue(factory.live().isEmpty())
        assertTrue(mixer.activeIds().isEmpty())
    }

    @Test
    fun stoppingWhilePreparing_preventsTheSoundFromStarting() = runTest {
        val mixer = mixer()
        factory.prepareDelayMs = 100

        launch { mixer.playSound(config("rain")) }
        testScheduler.advanceTimeBy(50)
        mixer.stopSound("rain")
        settle()

        assertTrue(factory.live().isEmpty())
        assertTrue(mixer.activeIds().isEmpty())
    }

    @Test
    fun stoppingLastSound_clearsState() = runTest {
        val mixer = mixer()
        mixer.playSound(config("rain"))

        mixer.stopSound("rain")

        assertFalse(mixer.state.value.isPlaying)
        assertTrue(mixer.activeIds().isEmpty())
        assertTrue(factory.live().isEmpty())
    }

    @Test
    fun playingNewSoundWhilePaused_resumesTheWholeMix() = runTest {
        val mixer = mixer()
        mixer.playSound(config("rain"))
        settle()
        mixer.pauseAll()

        mixer.playSound(config("fire"))
        settle()

        assertTrue(mixer.state.value.isPlaying)
        assertTrue(factory.live().values.all { it.isPlaying })
    }

    @Test
    fun pauseAndResume_toggleAllPlayers() = runTest {
        val mixer = mixer()
        mixer.playSound(config("rain"))
        mixer.playSound(config("fire"))
        settle()

        mixer.pauseAll()
        assertFalse(mixer.state.value.isPlaying)
        assertTrue(factory.live().values.none { it.isPlaying })

        mixer.resumeAll()
        assertTrue(mixer.state.value.isPlaying)
        assertTrue(factory.live().values.all { it.isPlaying })
    }

    @Test
    fun stopAll_releasesEverything() = runTest {
        val mixer = mixer()
        mixer.playSound(config("rain"))
        mixer.playSound(config("fire"))

        mixer.stopAll()

        assertTrue(factory.live().isEmpty())
        assertTrue(mixer.activeIds().isEmpty())
    }

    @Test
    fun setVolume_updatesPlayerAndState() = runTest {
        val mixer = mixer()
        mixer.playSound(config("rain"))

        mixer.setVolume("rain", 0.9f)

        assertEquals(0.9f, factory.live().getValue("rain").volume)
        assertEquals(0.9f, mixer.state.value.activeSounds.single().initialVolume)
    }

    @Test
    fun setMix_replacesSoundsNotInTheMix() = runTest {
        val mixer = mixer()
        mixer.playSound(config("rain"))

        mixer.setMix(listOf(config("fire"), config("wind")))
        settle()

        assertEquals(setOf("fire", "wind"), factory.live().keys)
        assertEquals(listOf("fire", "wind"), mixer.activeIds())
    }

    @Test
    fun setMix_withOneBrokenSound_playsTheRest() = runTest {
        val mixer = mixer()
        factory.failingIds += "broken"

        mixer.setMix(listOf(config("rain"), config("broken"), config("fire")))
        settle()

        assertEquals(setOf("rain", "fire"), factory.live().keys)
        assertEquals(listOf("rain", "fire"), mixer.activeIds())
    }

    @Test
    fun setMix_appliesNewVolumeToSoundsAlreadyPlaying() = runTest {
        val mixer = mixer()
        mixer.playSound(config("rain", volume = 0.2f))

        mixer.setMix(listOf(config("rain", volume = 0.8f)))

        assertEquals(0.8f, factory.live().getValue("rain").volume)
        assertEquals(0.8f, mixer.state.value.activeSounds.single().initialVolume)
    }

    @Test
    fun setMix_whilePaused_resumesPlayback() = runTest {
        val mixer = mixer()
        mixer.playSound(config("rain"))
        settle()
        mixer.pauseAll()

        mixer.setMix(listOf(config("rain"), config("fire")))
        settle()

        assertTrue(mixer.state.value.isPlaying)
        assertTrue(factory.live().values.all { it.isPlaying })
    }

    @Test
    fun setMix_respectsTheLimit() = runTest {
        val mixer = mixer()

        mixer.setMix((0 until AudioDefaults.MAX_CONCURRENT_SOUNDS + 2).map { config("s$it") })

        assertEquals(AudioDefaults.MAX_CONCURRENT_SOUNDS, factory.live().size)
        assertEquals(AudioDefaults.MAX_CONCURRENT_SOUNDS, mixer.activeIds().size)
    }

    @Test
    fun pausingRightAfterPlay_keepsTheSoundPaused() = runTest {
        val mixer = mixer()

        mixer.playSound(config("rain"))
        mixer.pauseAll() // fade-in has not started yet
        settle()

        assertFalse(mixer.state.value.isPlaying)
        assertFalse(factory.live().getValue("rain").isPlaying)
    }
}
