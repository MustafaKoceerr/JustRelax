package com.mustafakoceerr.justrelax.core.model.soundscape

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SceneTest {

    private fun bed(id: String, volume: Float = 0.5f) = Layer.Bed(id, volume = volume)
    private fun event(id: String, frequency: EventFrequency = EventFrequency.NORMAL) =
        Layer.Event(id, volume = 0.5f, frequency = frequency)

    @Test
    fun layersAreSplitIntoBedsAndEvents() {
        val scene = Scene(listOf(bed("wind"), event("owl"), bed("creek")))

        assertEquals(listOf("wind", "creek"), scene.beds.map { it.soundId })
        assertEquals(listOf("owl"), scene.events.map { it.soundId })
    }

    @Test
    fun addingALayer_appendsIt() {
        val scene = Scene.EMPTY.add(bed("wind")).add(event("owl"))

        assertEquals(listOf("wind", "owl"), scene.layers.map { it.soundId })
    }

    @Test
    fun theSameSoundCannotBeAddedTwice() {
        val scene = Scene.EMPTY.add(bed("wind"))

        assertFalse(scene.canAdd(bed("wind", volume = 0.9f)))
        assertFailsWith<IllegalArgumentException> { scene.add(bed("wind")) }
    }

    @Test
    fun aSceneHoldsAtMostTenLayers() {
        val full = (1..Scene.MAX_LAYERS).fold(Scene.EMPTY) { scene, i -> scene.add(bed("s$i")) }

        assertFalse(full.canAdd(event("owl")))
        assertFailsWith<IllegalArgumentException> { full.add(event("owl")) }
    }

    @Test
    fun removingALayer_dropsOnlyThatSound() {
        val scene = Scene(listOf(bed("wind"), event("owl")))

        assertEquals(listOf("owl"), scene.remove("wind").layers.map { it.soundId })
    }

    @Test
    fun volumeAndFrequencyCanBeChangedPerLayer() {
        val scene = Scene(listOf(bed("wind"), event("owl")))
            .withVolume("wind", 0.8f)
            .withFrequency("owl", EventFrequency.RARE)

        assertEquals(0.8f, scene.layer("wind")?.volume)
        assertEquals(EventFrequency.RARE, (scene.layer("owl") as Layer.Event).frequency)
    }

    @Test
    fun frequencyOnABed_isIgnored() {
        val scene = Scene(listOf(bed("wind"))).withFrequency("wind", EventFrequency.OFTEN)

        assertTrue(scene.layer("wind") is Layer.Bed)
    }

    @Test
    fun volumeIsKeptBetweenZeroAndOne() {
        val scene = Scene(listOf(bed("wind"))).withVolume("wind", 1.7f)

        assertEquals(1f, scene.layer("wind")?.volume)
        assertFailsWith<IllegalArgumentException> { bed("rain", volume = -0.1f) }
    }
}
