package com.mustafakoceerr.justrelax.core.playback.soundscape

import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
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
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class DefaultSoundscapeEngineTest {

    private val beds = FakeSoundPlayerFactory()
    private lateinit var events: FakeOneShotPlayer

    private suspend fun TestScope.engine(running: Boolean = true): DefaultSoundscapeEngine {
        events = FakeOneShotPlayer { testScheduler.currentTime }
        return DefaultSoundscapeEngine(beds, events, backgroundScope, Random(seed = 3), testTimeSource)
            .also { if (running) it.setRunning(true) }
    }

    private fun bed(id: String, volume: Float = 0.5f) = PlayableLayer.Bed(id, clip = clip(id), volume = volume)

    private fun event(id: String, volume: Float = 0.5f, frequency: EventFrequency = EventFrequency.OFTEN) =
        PlayableLayer.Event(id, clips = listOf("/sounds/${id}_1.ogg", "/sounds/${id}_2.ogg"), volume = volume, frequency = frequency)

    private suspend fun DefaultSoundscapeEngine.apply(vararg layers: PlayableLayer) = apply(PlayableScene(layers.toList()))

    private fun DefaultSoundscapeEngine.ids() = activeSoundIds.value

    private fun clip(id: String) = "/sounds/$id.ogg"

    /** advanceUntilIdle() skips backgroundScope work, where fade-ins run. */
    private fun TestScope.settle() {
        advanceUntilIdle()
        runCurrent()
    }

    // --- Beds ---

    @Test
    fun applyingABedWhileRunning_playsIt() = runTest {
        val engine = engine()

        engine.apply(bed("rain"))
        settle()

        assertEquals(setOf("rain"), engine.ids())
        assertTrue(beds.live().getValue(clip("rain")).isPlaying)
    }

    @Test
    fun bedsStartAtDifferentPoints_soTheirLoopsDontLineUp() = runTest {
        val engine = engine()

        engine.apply(bed("rain"), bed("wind"))
        settle()

        val starts = beds.live().values.map { it.startAtFraction!! }
        assertTrue(starts.all { it in 0f..1f })
        assertEquals(2, starts.distinct().size)
    }

    @Test
    fun applyingTheSameSceneTwice_keepsOnePlayer() = runTest {
        val engine = engine()

        engine.apply(bed("rain"))
        engine.apply(bed("rain"))

        assertEquals(1, beds.live().size)
    }

    @Test
    fun failedPrepare_releasesThePlayer_andSkipsTheLayer() = runTest {
        val engine = engine()
        beds.failingClips += clip("broken")

        engine.apply(bed("broken"))

        assertTrue(beds.live().isEmpty())
        assertTrue(engine.ids().isEmpty())
    }

    @Test
    fun aLayerRemovedWhilePreparing_neverStarts() = runTest {
        val engine = engine()
        beds.prepareDelayMs = 100

        launch { engine.apply(bed("rain")) }
        testScheduler.advanceTimeBy(50)
        engine.apply()
        settle()

        assertTrue(beds.live().isEmpty())
        assertTrue(engine.ids().isEmpty())
    }

    @Test
    fun applyingAnEmptyScene_releasesEverything() = runTest {
        val engine = engine()
        engine.apply(bed("rain"))

        engine.apply()

        assertTrue(engine.ids().isEmpty())
        assertTrue(beds.live().isEmpty())
    }

    @Test
    fun whileNotRunning_aSceneIsPreparedButSilent_untilRunning() = runTest {
        val engine = engine(running = false)

        engine.apply(bed("rain"))
        settle()
        assertFalse(beds.live().getValue(clip("rain")).isPlaying)

        engine.setRunning(true)
        settle()
        assertTrue(beds.live().getValue(clip("rain")).isPlaying)
    }

    @Test
    fun stoppingRightAfterApplying_keepsTheBedSilent() = runTest {
        val engine = engine()

        engine.apply(bed("rain"))
        engine.setRunning(false) // fade-in has not started yet
        settle()

        assertFalse(beds.live().getValue(clip("rain")).isPlaying)
    }

    @Test
    fun applyingWhilePaused_doesNotResume() = runTest {
        val engine = engine()
        engine.apply(bed("rain"))
        settle()
        engine.setRunning(false)

        engine.apply(bed("rain"), bed("fire"))
        settle()

        assertTrue(beds.live().values.none { it.isPlaying })
    }

    @Test
    fun apply_changesTheVolumeOfBedsAlreadyPlaying() = runTest {
        val engine = engine()
        engine.apply(bed("rain", volume = 0.2f))

        engine.apply(bed("rain", volume = 0.8f))

        assertEquals(0.8f, beds.live().getValue(clip("rain")).volume)
    }

    @Test
    fun apply_replacesLayersNotInTheScene() = runTest {
        val engine = engine()
        engine.apply(bed("rain"))

        engine.apply(bed("fire"), bed("wind"))
        settle()

        assertEquals(setOf(clip("fire"), clip("wind")), beds.live().keys)
        assertEquals(setOf("fire", "wind"), engine.ids())
    }

    @Test
    fun apply_withOneBrokenSound_playsTheRest() = runTest {
        val engine = engine()
        beds.failingClips += clip("broken")

        engine.apply(bed("rain"), bed("broken"), bed("fire"))
        settle()

        assertEquals(setOf(clip("rain"), clip("fire")), beds.live().keys)
        assertEquals(setOf("rain", "fire"), engine.ids())
    }

    @Test
    fun masterVolume_scalesEveryBed() = runTest {
        val engine = engine()
        engine.apply(bed("rain", volume = 0.5f))

        engine.setMasterVolume(0.2f)
        assertEquals(0.1f, beds.live().getValue(clip("rain")).volume!!, absoluteTolerance = 0.0001f)

        engine.setMasterVolume(1f)
        assertEquals(0.5f, beds.live().getValue(clip("rain")).volume)
    }

    @Test
    fun stop_releasesEverything() = runTest {
        val engine = engine()
        engine.apply(bed("rain"), event("owl"))

        engine.stop()

        assertTrue(beds.live().isEmpty())
        assertTrue(events.loaded.isEmpty())
        assertTrue(engine.ids().isEmpty())
    }

    // --- Events ---

    @Test
    fun anEvent_loadsItsClips_andPlaysThemOverTime() = runTest {
        val engine = engine()

        engine.apply(event("owl"))
        advanceTimeBy(5.minutes)
        runCurrent()

        assertEquals(listOf("/sounds/owl_1.ogg", "/sounds/owl_2.ogg"), events.loaded["owl"])
        assertTrue(events.plays.size > 5)
    }

    @Test
    fun anEventThatCannotLoad_isSkipped() = runTest {
        val engine = engine()
        events.failingSounds += "owl"

        engine.apply(event("owl"))

        assertTrue(engine.ids().isEmpty())
    }

    @Test
    fun removingAnEvent_stopsAndUnloadsIt() = runTest {
        val engine = engine()
        engine.apply(event("owl"))
        advanceTimeBy(1.minutes)

        engine.apply()
        val count = events.plays.size
        advanceTimeBy(10.minutes)
        runCurrent()

        assertEquals(count, events.plays.size)
        assertFalse("owl" in events.loaded)
    }

    @Test
    fun notRunning_silencesEvents_untilRunningAgain() = runTest {
        val engine = engine()
        engine.apply(event("owl"))
        advanceTimeBy(1.minutes)

        engine.setRunning(false)
        val count = events.plays.size
        advanceTimeBy(10.minutes)
        runCurrent()
        assertEquals(count, events.plays.size)
        assertTrue(events.isPaused)

        engine.setRunning(true)
        advanceTimeBy(1.minutes)
        runCurrent()
        assertTrue(events.plays.size > count)
        assertFalse(events.isPaused)
    }

    @Test
    fun aFrequencyChange_appliesToScheduling() = runTest {
        val engine = engine()
        engine.apply(event("owl", frequency = EventFrequency.OFTEN))

        engine.apply(event("owl", frequency = EventFrequency.RARE))
        val changedAt = testScheduler.currentTime
        advanceTimeBy(30.minutes)
        runCurrent()

        val gaps = events.plays.filter { it.atMs > changedAt }.drop(1).zipWithNext { a, b -> b.atMs - a.atMs }
        assertTrue(gaps.all { it >= EventFrequency.RARE.minIntervalMs }, "gaps: $gaps")
    }

    @Test
    fun anEventVolumeChange_appliesToItsNextOccurrences() = runTest {
        val engine = engine()
        engine.apply(event("owl", volume = 0.2f))

        engine.apply(event("owl", volume = 0.9f))
        runCurrent()
        val changedAt = testScheduler.currentTime
        advanceTimeBy(5.minutes)
        runCurrent()

        assertTrue(events.plays.filter { it.atMs > changedAt }.all { it.volume > 0.7f })
    }

    @Test
    fun masterVolume_scalesEvents() = runTest {
        val engine = engine()
        engine.apply(event("owl", volume = 0.9f))

        engine.setMasterVolume(0.2f)
        runCurrent()
        val changedAt = testScheduler.currentTime
        advanceTimeBy(5.minutes)
        runCurrent()

        assertTrue(events.plays.filter { it.atMs > changedAt }.all { it.volume < 0.3f })
    }

    @Test
    fun aSceneWithBedsAndEvents_playsBoth() = runTest {
        val engine = engine()

        engine.apply(bed("wind"), event("owl"), event("crackle"))
        advanceTimeBy(5.minutes)
        runCurrent()

        assertEquals(setOf(clip("wind")), beds.live().keys)
        assertEquals(setOf("owl", "crackle"), events.plays.map { it.soundId }.toSet())
        assertEquals(setOf("wind", "owl", "crackle"), engine.ids())
    }

    @Test
    fun anEventNeedsAtLeastOneClip() {
        assertFailsWith<IllegalArgumentException> {
            PlayableLayer.Event("owl", clips = emptyList(), volume = 0.5f, frequency = EventFrequency.NORMAL)
        }
    }
}
