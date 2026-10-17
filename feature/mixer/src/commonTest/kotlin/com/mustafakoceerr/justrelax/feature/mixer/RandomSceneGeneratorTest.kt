package com.mustafakoceerr.justrelax.feature.mixer

import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.core.testing.testSound
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RandomSceneGeneratorTest {

    private val beds = (1..4).map { testSound("bed$it", localPath = "/b$it.ogg") }
    private val events = (1..5).map {
        testSound("event$it", localPath = "/e$it.ogg", kind = SoundKind.EVENT, defaultFrequency = EventFrequency.OFTEN)
    }
    private val notDownloaded = listOf(testSound("cloud"), testSound("cloud_event", kind = SoundKind.EVENT))
    private val library = beds + events + notDownloaded

    private fun generate(layerCount: Int, sounds: List<com.mustafakoceerr.justrelax.core.model.Sound> = library, seed: Int = 1) =
        RandomSceneGenerator(Random(seed)).generate(sounds, layerCount)

    @Test
    fun aSmallScene_isOneBedAndEvents() {
        val scene = generate(layerCount = 3)

        assertEquals(1, scene.beds.size)
        assertEquals(2, scene.events.size)
    }

    @Test
    fun theLargestScene_isTwoBedsAndThreeEvents() {
        val scene = generate(layerCount = 5)

        assertEquals(2, scene.beds.size)
        assertEquals(3, scene.events.size)
    }

    @Test
    fun onlyDownloadedSoundsAreUsed() {
        repeat(20) { seed ->
            val ids = generate(layerCount = 5, seed = seed).layers.map { it.soundId }
            assertTrue(ids.none { it.startsWith("cloud") }, "seed $seed: $ids")
        }
    }

    @Test
    fun withoutEvents_bedsFillTheScene_andTheOtherWayRound() {
        assertEquals(4, generate(layerCount = 4, sounds = beds).beds.size)
        assertEquals(3, generate(layerCount = 3, sounds = events).events.size)
    }

    @Test
    fun events_playAtTheirDefaultFrequency_andVolumesStayInRange() {
        repeat(20) { seed ->
            val scene = generate(layerCount = 5, seed = seed)
            assertTrue(scene.events.all { it.frequency == EventFrequency.OFTEN })
            assertTrue(scene.layers.all { it.volume in 0.3f..0.8f }, "seed $seed")
        }
    }

    @Test
    fun theSameSeed_givesTheSameScene() {
        assertEquals(generate(layerCount = 4, seed = 7), generate(layerCount = 4, seed = 7))
    }

    @Test
    fun anEmptyLibrary_givesAnEmptyScene() {
        assertTrue(generate(layerCount = 3, sounds = notDownloaded).layers.isEmpty())
    }

    @Test
    fun aLayerIsABedOrAnEventAccordingToItsSound() {
        val scene = generate(layerCount = 5)

        assertTrue(scene.layers.filterIsInstance<Layer.Bed>().all { it.soundId.startsWith("bed") })
        assertTrue(scene.layers.filterIsInstance<Layer.Event>().all { it.soundId.startsWith("event") })
    }
}
