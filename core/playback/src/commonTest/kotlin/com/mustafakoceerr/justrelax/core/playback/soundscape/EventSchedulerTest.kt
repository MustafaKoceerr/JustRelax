package com.mustafakoceerr.justrelax.core.playback.soundscape

import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.testTimeSource
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class EventSchedulerTest {

    private data class Play(val atMs: Long, val soundId: String, val clip: String, val volume: Float, val pan: Float)

    private class RecordingPlayer(private val now: () -> Long) : OneShotPlayer {
        val plays = mutableListOf<Play>()
        override fun play(soundId: String, clip: String, volume: Float, pan: Float) {
            plays += Play(now(), soundId, clip, volume, pan)
        }

        override suspend fun load(soundId: String, clips: List<String>) = Unit
        override fun unload(soundId: String) = Unit
        override fun pauseAll() = Unit
        override fun resumeAll() = Unit
        override fun stopAll() = Unit
    }

    private fun TestScope.scheduler(player: RecordingPlayer) =
        EventScheduler(backgroundScope, player, Random(seed = 7), testTimeSource)

    private fun TestScope.recorder() = RecordingPlayer { testScheduler.currentTime }

    private fun TestScope.run(duration: Duration) {
        advanceTimeBy(duration)
        runCurrent()
    }

    private fun owl(frequency: EventFrequency = EventFrequency.OFTEN, volume: Float = 0.5f, clips: Int = 3) =
        PlayableLayer.Event("owl", clips = (1..clips).map { "/owl_$it.ogg" }, volume = volume, frequency = frequency)

    private fun List<Play>.gaps() = zipWithNext { a, b -> b.atMs - a.atMs }

    @Test
    fun anEventPlaysRepeatedly_atIntervalsWithinItsFrequency() = runTest {
        val player = recorder()
        scheduler(player).sync(listOf(owl(EventFrequency.OFTEN)))

        run(10.minutes)

        assertTrue(player.plays.size > 20, "plays: ${player.plays.size}")
        assertTrue(player.plays.first().atMs <= EventFrequency.OFTEN.minIntervalMs)
        assertTrue(player.plays.gaps().all { it in EventFrequency.OFTEN.minIntervalMs..EventFrequency.OFTEN.maxIntervalMs })
    }

    @Test
    fun theSameClipIsNeverPlayedTwiceInARow() = runTest {
        val player = recorder()
        scheduler(player).sync(listOf(owl(clips = 3)))

        run(30.minutes)

        assertTrue(player.plays.zipWithNext().none { (a, b) -> a.clip == b.clip })
        assertEquals(3, player.plays.map { it.clip }.distinct().size)
    }

    @Test
    fun aSingleClipEventStillPlays() = runTest {
        val player = recorder()
        scheduler(player).sync(listOf(owl(clips = 1)))

        run(5.minutes)

        assertTrue(player.plays.isNotEmpty())
        assertTrue(player.plays.all { it.clip == "/owl_1.ogg" })
    }

    @Test
    fun volumeAndStereoPositionVary_withinLimits() = runTest {
        val player = recorder()
        scheduler(player).sync(listOf(owl(volume = 0.6f)))

        run(30.minutes)

        val volumes = player.plays.map { it.volume }
        val pans = player.plays.map { it.pan }
        assertTrue(volumes.all { it in 0.6f * 0.85f..0.6f * 1.15f }, "volumes: $volumes")
        assertTrue(pans.all { it in -0.6f..0.6f }, "pans: $pans")
        assertTrue(volumes.distinct().size > 1 && pans.distinct().size > 1)
    }

    @Test
    fun jitteredVolume_neverExceedsOne() = runTest {
        val player = recorder()
        scheduler(player).sync(listOf(owl(volume = 1f)))

        run(30.minutes)

        assertTrue(player.plays.all { it.volume <= 1f })
    }

    @Test
    fun pause_freezesTheWait_andResumeContinuesWhereItLeftOff() = runTest {
        val player = recorder()
        val scheduler = scheduler(player)
        scheduler.sync(listOf(owl(EventFrequency.OFTEN)))
        run(1.minutes)
        val beforePause = player.plays.size

        scheduler.pause()
        run(10.minutes)
        assertEquals(beforePause, player.plays.size)

        scheduler.resume()
        run(EventFrequency.OFTEN.maxIntervalMs.milliseconds + 1.seconds)
        assertTrue(player.plays.size > beforePause)
        // The gap spanning the pause, minus the pause itself, is still a normal interval.
        val spanning = player.plays[beforePause].atMs - player.plays[beforePause - 1].atMs - 10.minutes.inWholeMilliseconds
        assertTrue(spanning in EventFrequency.OFTEN.minIntervalMs..EventFrequency.OFTEN.maxIntervalMs, "gap: $spanning")
    }

    @Test
    fun removingALayer_stopsItsEvents() = runTest {
        val player = recorder()
        val scheduler = scheduler(player)
        scheduler.sync(listOf(owl()))
        run(2.minutes)
        val count = player.plays.size

        scheduler.sync(emptyList())
        run(10.minutes)

        assertEquals(count, player.plays.size)
    }

    @Test
    fun frequencyChange_appliesFromTheNextWait() = runTest {
        val player = recorder()
        val scheduler = scheduler(player)
        scheduler.sync(listOf(owl(EventFrequency.OFTEN)))
        run(2.minutes)

        scheduler.sync(listOf(owl(EventFrequency.RARE)))
        val changedAt = testScheduler.currentTime
        run(30.minutes)

        // Skip the wait that was already running when the frequency changed.
        val afterChange = player.plays.filter { it.atMs > changedAt }.drop(1)
        assertTrue(afterChange.gaps().all { it >= EventFrequency.RARE.minIntervalMs }, "gaps: ${afterChange.gaps()}")
    }

    @Test
    fun volumeChange_appliesToTheNextPlay() = runTest {
        val player = recorder()
        val scheduler = scheduler(player)
        scheduler.sync(listOf(owl(volume = 0.2f)))
        run(1.minutes)

        scheduler.sync(listOf(owl(volume = 0.9f)))
        val changedAt = testScheduler.currentTime
        run(2.minutes)

        assertTrue(player.plays.filter { it.atMs > changedAt }.all { it.volume > 0.7f })
    }

    @Test
    fun eachEventLayerRunsIndependently() = runTest {
        val player = recorder()
        val crackle = PlayableLayer.Event("crackle", listOf("/crackle.ogg"), volume = 0.5f, frequency = EventFrequency.OFTEN)
        scheduler(player).sync(listOf(owl(EventFrequency.RARE), crackle))

        run(20.minutes)

        val owls = player.plays.filter { it.soundId == "owl" }
        val crackles = player.plays.filter { it.soundId == "crackle" }
        assertTrue(crackles.size > owls.size * 2, "owls ${owls.size}, crackles ${crackles.size}")
        assertTrue(owls.gaps().all { it >= EventFrequency.RARE.minIntervalMs })
    }

    @Test
    fun stop_cancelsEverything() = runTest {
        val player = recorder()
        val scheduler = scheduler(player)
        scheduler.sync(listOf(owl()))
        run(1.minutes)
        val count = player.plays.size

        scheduler.stop()
        run(10.minutes)

        assertEquals(count, player.plays.size)
    }
}
