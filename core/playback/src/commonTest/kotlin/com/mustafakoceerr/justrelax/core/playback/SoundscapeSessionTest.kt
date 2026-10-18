package com.mustafakoceerr.justrelax.core.playback

import com.mustafakoceerr.justrelax.core.data.repository.InMemorySceneRepository
import com.mustafakoceerr.justrelax.core.playback.SoundscapeSession.PlayWhenReadyReason
import com.mustafakoceerr.justrelax.core.playback.soundscape.PlayableLayer
import com.mustafakoceerr.justrelax.core.playback.soundscape.PlayableScene
import com.mustafakoceerr.justrelax.core.playback.soundscape.SoundscapeEngine
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSleepTimer
import com.mustafakoceerr.justrelax.core.testing.fake.FakeSoundRepository
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.core.testing.testClip
import com.mustafakoceerr.justrelax.core.testing.testSound
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class SoundscapeSessionTest {

    private class FakeEngine : SoundscapeEngine {
        override val activeSoundIds = MutableStateFlow(emptySet<String>())
        /** The dispatcher of every call, to check where the engine's work runs. */
        val callDispatchers = mutableSetOf<ContinuationInterceptor?>()
        val applied = mutableListOf<PlayableScene>()
        var running = false
        var masterVolume = 1f
        val masterVolumes = mutableListOf<Float>()
        var stopped = false

        override suspend fun apply(scene: PlayableScene) {
            recordDispatcher()
            applied += scene
        }

        override suspend fun setRunning(running: Boolean) {
            recordDispatcher()
            this.running = running
        }

        override suspend fun setMasterVolume(volume: Float) {
            recordDispatcher()
            masterVolume = volume
            masterVolumes += volume
        }

        override suspend fun stop() {
            recordDispatcher()
            stopped = true
        }

        private suspend fun recordDispatcher() {
            callDispatchers += currentCoroutineContext()[ContinuationInterceptor]
        }
    }

    private val sounds = FakeSoundRepository(
        listOf(
            testSound("rain", localPath = "/sounds/rain.mp3"),
            testSound("fire", localPath = "/sounds/fire.mp3"),
            testSound("cloud"),
            testSound(
                "birds",
                kind = SoundKind.EVENT,
                defaultFrequency = EventFrequency.OFTEN,
                clips = (0..2).map { testClip("https://cdn.test/birds_$it.ogg", "/sounds/birds_$it.ogg") },
            ),
        )
    )
    private val scene = InMemorySceneRepository(sounds)
    private val engine = FakeEngine()
    private val timer = FakeSleepTimer()

    private fun TestScope.session() =
        SoundscapeSession(scene, sounds, { engine }, timer, StandardTestDispatcher(testScheduler)).also { runCurrent() }

    private suspend fun TestScope.play(vararg ids: String) {
        ids.forEach { scene.toggle(it) }
        runCurrent()
    }

    @Test
    fun theEngineWorks_onItsOwnDispatcher() = runTest {
        val sessionDispatcher = StandardTestDispatcher(testScheduler, name = "session")
        val engineDispatcher = StandardTestDispatcher(testScheduler, name = "engine")
        var engineScopeDispatcher: ContinuationInterceptor? = null
        val session = SoundscapeSession(
            sceneRepository = scene,
            soundRepository = sounds,
            engineFactory = { scope -> engine.also { engineScopeDispatcher = scope.coroutineContext[ContinuationInterceptor] } },
            sleepTimer = timer,
            context = sessionDispatcher,
            engineContext = engineDispatcher,
        )
        runCurrent()

        play("rain")
        session.setPlayWhenReady(true)
        scene.setVolume("rain", 0.2f)
        runCurrent()
        session.release()
        runCurrent()

        assertEquals(engineDispatcher, engineScopeDispatcher)
        assertEquals(setOf<ContinuationInterceptor?>(engineDispatcher), engine.callDispatchers)
        assertTrue(engine.stopped)
    }

    @Test
    fun theSceneIsResolvedToLocalFiles_andApplied() = runTest {
        session()

        scene.replaceWith(mapOf("rain" to 0.4f, "cloud" to 0.5f))
        runCurrent()

        assertEquals(
            PlayableScene(listOf(PlayableLayer.Bed("rain", "/sounds/rain.mp3", 0.4f))),
            engine.applied.last(),
        )
    }

    @Test
    fun anEventSound_playsEveryOneOfItsClips() = runTest {
        session()

        play("birds")

        assertEquals(
            PlayableLayer.Event(
                soundId = "birds",
                clips = (0..2).map { "/sounds/birds_$it.ogg" },
                volume = 0.5f,
                frequency = EventFrequency.OFTEN,
            ),
            engine.applied.last().layers.single(),
        )
    }

    @Test
    fun aSoundMissingAClip_isLeftOut() = runTest {
        session()
        play("birds")

        sounds.sounds.value = sounds.sounds.value.map { sound ->
            if (sound.id == "birds") sound.copy(clips = sound.clips.dropLast(1) + testClip("https://cdn.test/birds_2.ogg")) else sound
        }
        runCurrent()

        assertTrue(engine.applied.last().layers.isEmpty())
    }

    @Test
    fun sceneEdits_reachTheEngine() = runTest {
        session()
        play("rain")

        scene.setVolume("rain", 0.9f)
        runCurrent()

        assertEquals(0.9f, engine.applied.last().layers.single().volume)
    }

    @Test
    fun theEngineRuns_onlyWhenPlayWhenReady_andNotSuppressed() = runTest {
        val session = session()
        play("rain")
        assertFalse(engine.running)

        session.setPlayWhenReady(true)
        runCurrent()
        assertTrue(engine.running)

        session.setSuppressed(true)
        runCurrent()
        assertFalse(engine.running)

        session.setSuppressed(false)
        runCurrent()
        assertTrue(engine.running)
    }

    @Test
    fun clearingTheScene_stopsPlayback_andTheSleepTimer() = runTest {
        val session = session()
        play("rain")
        session.setPlayWhenReady(true)
        runCurrent()

        scene.clear()
        runCurrent()

        val state = session.state.value
        assertFalse(state.hasContent)
        assertFalse(state.playWhenReady)
        assertEquals(PlayWhenReadyReason.SCENE_CLEARED, state.reason)
        assertFalse(engine.running)
        assertEquals(1, timer.cancelCount)
    }

    @Test
    fun theSleepTimer_fadesOut_thenPauses() = runTest {
        val session = session()
        play("rain")
        session.setPlayWhenReady(true)
        runCurrent()

        timer.expire()
        advanceTimeBy(SoundscapeSession.FADE_OUT_DURATION / 2)
        runCurrent()
        assertTrue(engine.masterVolume in 0.3f..0.7f, "volume ${engine.masterVolume}")
        assertTrue(session.state.value.playWhenReady)

        advanceTimeBy(SoundscapeSession.FADE_OUT_DURATION)
        runCurrent()
        assertFalse(session.state.value.playWhenReady)
        assertEquals(PlayWhenReadyReason.SLEEP_TIMER, session.state.value.reason)
        assertFalse(engine.running)
        assertEquals(1f, engine.masterVolume) // ready for the next play
    }

    @Test
    fun playingDuringTheFade_cancelsIt_andRestoresTheVolume() = runTest {
        val session = session()
        play("rain")
        session.setPlayWhenReady(true)
        runCurrent()
        timer.expire()
        advanceTimeBy(3.seconds)

        session.setPlayWhenReady(true)
        advanceTimeBy(SoundscapeSession.FADE_OUT_DURATION)
        runCurrent()

        assertTrue(session.state.value.playWhenReady)
        assertEquals(1f, engine.masterVolume)
    }

    @Test
    fun ducking_scalesTheMasterVolume() = runTest {
        val session = session()

        session.setDuckVolume(0.2f)
        runCurrent()

        assertEquals(0.2f, engine.masterVolume)
    }

    @Test
    fun release_stopsTheEngine_butKeepsTheScene() = runTest {
        val session = session()
        play("rain")

        session.release()
        runCurrent()

        assertTrue(engine.stopped)
        assertEquals(listOf("rain"), scene.scene.value.layers.map { it.soundId })
    }

    @Test
    fun anInterruption_suppressesPlayback_andItsEndResumesWhenTheSystemSaysSo() = runTest {
        val session = session()
        play("rain")
        session.setPlayWhenReady(true)

        session.onAudioSessionEvent(AudioSessionEvent.InterruptionBegan)
        runCurrent()
        assertFalse(engine.running)
        assertTrue(session.state.value.playWhenReady)

        session.onAudioSessionEvent(AudioSessionEvent.InterruptionEnded(shouldResume = true))
        runCurrent()
        assertTrue(engine.running)
    }

    @Test
    fun anInterruptionThatEndsWithoutResume_pauses() = runTest {
        val session = session()
        play("rain")
        session.setPlayWhenReady(true)

        session.onAudioSessionEvent(AudioSessionEvent.InterruptionBegan)
        session.onAudioSessionEvent(AudioSessionEvent.InterruptionEnded(shouldResume = false))
        runCurrent()

        assertFalse(engine.running)
        assertEquals(SoundscapeSession.State(hasContent = true, reason = PlayWhenReadyReason.AUDIO_FOCUS_LOSS), session.state.value)
    }

    @Test
    fun anInterruptionWhilePaused_changesNothing() = runTest {
        val session = session()
        play("rain")

        session.onAudioSessionEvent(AudioSessionEvent.InterruptionBegan)
        session.onAudioSessionEvent(AudioSessionEvent.InterruptionEnded(shouldResume = true))
        runCurrent()

        assertEquals(SoundscapeSession.State(hasContent = true), session.state.value)
    }

    @Test
    fun losingTheOutput_pausesPlayback() = runTest {
        val session = session()
        play("rain")
        session.setPlayWhenReady(true)

        session.onAudioSessionEvent(AudioSessionEvent.OutputLost)
        runCurrent()

        assertFalse(engine.running)
        assertEquals(PlayWhenReadyReason.BECOMING_NOISY, session.state.value.reason)
    }
}
