package com.mustafakoceerr.justrelax.core.playback.soundscape

import android.app.Application
import android.media.SoundPool
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSoundPool
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class AndroidEventPlayerTest {

    private val soundPool = AndroidEventPlayer.createSoundPool()
    private val shadow: ShadowSoundPool = shadowOf(soundPool)
    private val player = AndroidEventPlayer(soundPool)

    private val birds = listOf("/audio/birds_0.ogg", "/audio/birds_1.ogg")

    @AfterTest
    fun tearDown() = player.release()

    @Test
    fun load_waitsUntilEveryClipIsDecoded() = runTest {
        var loaded = false
        launch { player.load("birds", birds); loaded = true }
        runCurrent()

        shadow.notifyPathLoaded(birds[0], true)
        runCurrent()
        assertFalse(loaded)

        shadow.notifyPathLoaded(birds[1], true)
        runCurrent()
        assertTrue(loaded)
    }

    @Test
    fun aClipThatCannotBeDecoded_failsTheLoad() = runTest {
        val load = launch {
            assertFailsWith<IllegalStateException> { player.load("birds", birds) }
        }
        runCurrent()

        shadow.notifyPathLoaded(birds[0], true)
        shadow.notifyPathLoaded(birds[1], false)
        load.join()

        player.play("birds", birds[0], volume = 1f, pan = 0f)
        assertFalse(shadow.wasPathPlayed(birds[0]))
    }

    @Test
    fun play_playsTheClipOnce_atItsStereoVolumes() = runTest {
        loadBirds()

        player.play("birds", birds[1], volume = 0.8f, pan = 0.5f)

        val playback = shadow.getPathPlaybacks(birds[1]).single()
        assertEquals(0.4f, playback.leftVolume, 0.001f)
        assertEquals(0.8f, playback.rightVolume, 0.001f)
        assertEquals(0, playback.loop)
        assertFalse(shadow.wasPathPlayed(birds[0]))
    }

    @Test
    fun anUnloadedSound_isNotPlayed() = runTest {
        loadBirds()

        player.unload("birds")
        player.play("birds", birds[0], volume = 1f, pan = 0f)

        assertFalse(shadow.wasPathPlayed(birds[0]))
    }

    private suspend fun kotlinx.coroutines.test.TestScope.loadBirds() {
        val load = launch { player.load("birds", birds) }
        runCurrent()
        birds.forEach { shadow.notifyPathLoaded(it, true) }
        load.join()
    }
}
