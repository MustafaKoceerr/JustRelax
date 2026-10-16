package com.mustafakoceerr.justrelax.core.playback.soundscape

import com.mustafakoceerr.justrelax.core.playback.AudioServiceController
import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.testTimeSource
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class DefaultSoundscapeEngineTest {

    private val beds = FakeSoundPlayerFactory()
    private var serviceStarts = 0
    private val serviceController = AudioServiceController { serviceStarts++ }
    private lateinit var events: FakeOneShotPlayer

    private fun TestScope.engine(): DefaultSoundscapeEngine {
        events = FakeOneShotPlayer { testScheduler.currentTime }
        return DefaultSoundscapeEngine(beds, events, serviceController, backgroundScope, Random(seed = 3), testTimeSource)
    }

    private fun bed(id: String, volume: Float = 0.5f) = Layer.Bed(id, clip = "/sounds/$id.ogg", volume = volume)

    private fun event(id: String, volume: Float = 0.5f, frequency: EventFrequency = EventFrequency.OFTEN) =
        Layer.Event(id, clips = listOf("/sounds/${id}_1.ogg", "/sounds/${id}_2.ogg"), volume = volume, frequency = frequency)

    private fun DefaultSoundscapeEngine.ids() = state.value.layers.map { it.soundId }

    private fun clip(id: String) = "/sounds/$id.ogg"

    /** advanceUntilIdle() skips backgroundScope work, where fade-ins run. */
    private fun TestScope.settle() {
        advanceUntilIdle()
        runCurrent()
    }

    // --- Beds (behaviour carried over from the previous mixer) ---

    @Test
    fun addingABed_startsServiceAndPlays() = runTest {
        val engine = engine()

        engine.addLayer(bed("rain"))
        settle()

        assertEquals(1, serviceStarts)
        assertTrue(engine.state.value.isPlaying)
        assertEquals(listOf("rain"), engine.ids())
        assertTrue(beds.live().getValue(clip("rain")).isPlaying)
    }

    @Test
    fun bedsStartAtDifferentPoints_soTheirLoopsDontLineUp() = runTest {
        val engine = engine()

        engine.addLayer(bed("rain"))
        engine.addLayer(bed("wind"))
        settle()

        val starts = beds.live().values.map { it.startAtFraction!! }
        assertTrue(starts.all { it in 0f..1f })
        assertEquals(2, starts.distinct().size)
    }

    @Test
    fun addingTheSameSoundTwice_keepsOneLayer() = runTest {
        val engine = engine()

        engine.addLayer(bed("rain"))
        engine.addLayer(bed("rain"))

        assertEquals(1, beds.live().size)
        assertEquals(listOf("rain"), engine.ids())
    }

    @Test
    fun addingBeyondTheLimit_returnsLimitExceeded() = runTest {
        val engine = engine()
        repeat(Scene.MAX_LAYERS) { engine.addLayer(bed("s$it")) }

        val result = engine.addLayer(event("owl"))

        assertIs<AppError.Player.LimitExceeded>(assertIs<Resource.Error>(result).error)
        assertEquals(Scene.MAX_LAYERS, engine.ids().size)
    }

    @Test
    fun concurrentAdds_neverExceedTheLimit() = runTest {
        val engine = engine()
        beds.prepareDelayMs = 100

        repeat(Scene.MAX_LAYERS + 3) { launch { engine.addLayer(bed("s$it")) } }
        settle()

        assertEquals(Scene.MAX_LAYERS, beds.live().size)
        assertEquals(Scene.MAX_LAYERS, engine.ids().size)
    }

    @Test
    fun failedPrepare_returnsError_andReleasesPlayer() = runTest {
        val engine = engine()
        beds.failingClips += clip("broken")

        val result = engine.addLayer(bed("broken"))

        assertIs<Resource.Error>(result)
        assertTrue(beds.live().isEmpty())
        assertTrue(engine.ids().isEmpty())
    }

    @Test
    fun removingWhilePreparing_preventsTheSoundFromStarting() = runTest {
        val engine = engine()
        beds.prepareDelayMs = 100

        launch { engine.addLayer(bed("rain")) }
        testScheduler.advanceTimeBy(50)
        engine.removeLayer("rain")
        settle()

        assertTrue(beds.live().isEmpty())
        assertTrue(engine.ids().isEmpty())
    }

    @Test
    fun removingTheLastLayer_clearsState() = runTest {
        val engine = engine()
        engine.addLayer(bed("rain"))

        engine.removeLayer("rain")

        assertFalse(engine.state.value.isPlaying)
        assertTrue(engine.ids().isEmpty())
        assertTrue(beds.live().isEmpty())
    }

    @Test
    fun addingWhilePaused_resumesTheWholeScene() = runTest {
        val engine = engine()
        engine.addLayer(bed("rain"))
        settle()
        engine.pauseAll()

        engine.addLayer(bed("fire"))
        settle()

        assertTrue(engine.state.value.isPlaying)
        assertTrue(beds.live().values.all { it.isPlaying })
    }

    @Test
    fun pausingRightAfterAdding_keepsTheBedPaused() = runTest {
        val engine = engine()

        engine.addLayer(bed("rain"))
        engine.pauseAll() // fade-in has not started yet
        settle()

        assertFalse(engine.state.value.isPlaying)
        assertFalse(beds.live().getValue(clip("rain")).isPlaying)
    }

    @Test
    fun setVolume_updatesPlayerAndState() = runTest {
        val engine = engine()
        engine.addLayer(bed("rain"))

        engine.setVolume("rain", 0.9f)

        assertEquals(0.9f, beds.live().getValue(clip("rain")).volume)
        assertEquals(0.9f, engine.state.value.layers.single().volume)
    }

    @Test
    fun play_replacesLayersNotInTheScene() = runTest {
        val engine = engine()
        engine.addLayer(bed("rain"))

        engine.play(Scene(listOf(bed("fire"), bed("wind"))))
        settle()

        assertEquals(setOf(clip("fire"), clip("wind")), beds.live().keys)
        assertEquals(listOf("fire", "wind"), engine.ids())
    }

    @Test
    fun play_withOneBrokenSound_playsTheRest() = runTest {
        val engine = engine()
        beds.failingClips += clip("broken")

        engine.play(Scene(listOf(bed("rain"), bed("broken"), bed("fire"))))
        settle()

        assertEquals(setOf(clip("rain"), clip("fire")), beds.live().keys)
        assertEquals(listOf("rain", "fire"), engine.ids())
    }

    @Test
    fun play_appliesNewVolumeToLayersAlreadyPlaying() = runTest {
        val engine = engine()
        engine.addLayer(bed("rain", volume = 0.2f))

        engine.play(Scene(listOf(bed("rain", volume = 0.8f))))

        assertEquals(0.8f, beds.live().getValue(clip("rain")).volume)
        assertEquals(0.8f, engine.state.value.layers.single().volume)
    }

    @Test
    fun play_whilePaused_resumesPlayback() = runTest {
        val engine = engine()
        engine.addLayer(bed("rain"))
        settle()
        engine.pauseAll()

        engine.play(Scene(listOf(bed("rain"), bed("fire"))))
        settle()

        assertTrue(engine.state.value.isPlaying)
        assertTrue(beds.live().values.all { it.isPlaying })
    }

    @Test
    fun stopAll_releasesEverything() = runTest {
        val engine = engine()
        engine.addLayer(bed("rain"))
        engine.addLayer(event("owl"))

        engine.stopAll()

        assertTrue(beds.live().isEmpty())
        assertTrue(events.loaded.isEmpty())
        assertTrue(engine.ids().isEmpty())
    }

    // --- Events ---

    @Test
    fun addingAnEvent_loadsItsClips_andPlaysThemOverTime() = runTest {
        val engine = engine()

        engine.addLayer(event("owl"))
        advanceTimeBy(5.minutes)
        runCurrent()

        assertEquals(listOf("/sounds/owl_1.ogg", "/sounds/owl_2.ogg"), events.loaded["owl"])
        assertTrue(events.plays.size > 5)
        assertEquals(1, serviceStarts)
        assertTrue(engine.state.value.isPlaying)
    }

    @Test
    fun anEventThatCannotLoad_isNotAdded() = runTest {
        val engine = engine()
        events.failingSounds += "owl"

        val result = engine.addLayer(event("owl"))

        assertIs<Resource.Error>(result)
        assertTrue(engine.ids().isEmpty())
    }

    @Test
    fun removingAnEvent_stopsAndUnloadsIt() = runTest {
        val engine = engine()
        engine.addLayer(event("owl"))
        advanceTimeBy(1.minutes)

        engine.removeLayer("owl")
        val count = events.plays.size
        advanceTimeBy(10.minutes)
        runCurrent()

        assertEquals(count, events.plays.size)
        assertFalse("owl" in events.loaded)
    }

    @Test
    fun pause_silencesEventsUntilResumed() = runTest {
        val engine = engine()
        engine.addLayer(event("owl"))
        advanceTimeBy(1.minutes)

        engine.pauseAll()
        val count = events.plays.size
        advanceTimeBy(10.minutes)
        runCurrent()
        assertEquals(count, events.plays.size)
        assertTrue(events.isPaused)

        engine.resumeAll()
        advanceTimeBy(1.minutes)
        runCurrent()
        assertTrue(events.plays.size > count)
        assertFalse(events.isPaused)
    }

    @Test
    fun setFrequency_isReflectedInStateAndScheduling() = runTest {
        val engine = engine()
        engine.addLayer(event("owl", frequency = EventFrequency.OFTEN))

        engine.setFrequency("owl", EventFrequency.RARE)
        val changedAt = testScheduler.currentTime
        advanceTimeBy(30.minutes)
        runCurrent()

        assertEquals(EventFrequency.RARE, (engine.state.value.layers.single() as Layer.Event).frequency)
        val gaps = events.plays.filter { it.atMs > changedAt }.drop(1).zipWithNext { a, b -> b.atMs - a.atMs }
        assertTrue(gaps.all { it >= EventFrequency.RARE.minIntervalMs }, "gaps: $gaps")
    }

    @Test
    fun eventVolumeChange_appliesToItsNextOccurrences() = runTest {
        val engine = engine()
        engine.addLayer(event("owl", volume = 0.2f))

        engine.setVolume("owl", 0.9f)
        runCurrent()
        val changedAt = testScheduler.currentTime
        advanceTimeBy(5.minutes)
        runCurrent()

        assertTrue(events.plays.filter { it.atMs > changedAt }.all { it.volume > 0.7f })
    }

    @Test
    fun aSceneWithBedsAndEvents_playsBoth() = runTest {
        val engine = engine()

        engine.play(Scene(listOf(bed("wind"), event("owl"), event("crackle"))))
        advanceTimeBy(5.minutes)
        runCurrent()

        assertEquals(setOf(clip("wind")), beds.live().keys)
        assertEquals(setOf("owl", "crackle"), events.plays.map { it.soundId }.toSet())
        assertEquals(listOf("wind", "owl", "crackle"), engine.ids())
    }
}
