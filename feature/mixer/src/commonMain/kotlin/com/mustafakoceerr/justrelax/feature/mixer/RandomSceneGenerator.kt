package com.mustafakoceerr.justrelax.feature.mixer

import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import kotlin.random.Random

/**
 * Builds a random scene the way a sound designer would: one or two beds to carry it and up to
 * three events on top. If the library has too few of one kind, the other fills the scene.
 */
class RandomSceneGenerator(private val random: Random = Random) {

    fun generate(sounds: List<Sound>, layerCount: Int): Scene {
        val downloaded = sounds.filter { it.isDownloaded }
        val beds = downloaded.filter { it.kind == SoundKind.BED }.shuffled(random)
        val events = downloaded.filter { it.kind == SoundKind.EVENT }.shuffled(random)

        val wantedBeds = if (layerCount >= TWO_BEDS_FROM) 2 else 1
        val pickedEvents = events.take(minOf(MAX_EVENTS, layerCount - wantedBeds))
        val pickedBeds = beds.take(layerCount - pickedEvents.size)
        val moreEvents = events.drop(pickedEvents.size).take(layerCount - pickedBeds.size - pickedEvents.size)

        return Scene(
            (pickedBeds.map { Layer.Bed(it.id, randomVolume()) } +
                (pickedEvents + moreEvents).map { Layer.Event(it.id, randomVolume(), it.defaultFrequency) })
                .take(Scene.MAX_LAYERS)
        )
    }

    private fun randomVolume(): Float = MIN_VOLUME + random.nextFloat() * (MAX_VOLUME - MIN_VOLUME)

    companion object {
        const val MIN_LAYERS = 2
        const val MAX_LAYERS = 5
        private const val TWO_BEDS_FROM = 4
        private const val MAX_EVENTS = 3
        private const val MIN_VOLUME = 0.3f
        private const val MAX_VOLUME = 0.8f
    }
}
