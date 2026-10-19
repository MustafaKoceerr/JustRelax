package com.mustafakoceerr.justrelax.feature.mixer

import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.soundscape.Layer
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import kotlin.random.Random

/**
 * Builds a random scene the way a sound designer would: one or two beds to carry it and up to
 * three events on top. If the library has too few of one kind, the other fills the scene.
 * Layers in [keep][generate] stay as they are and count towards those numbers.
 */
class RandomSceneGenerator(private val random: Random = Random) {

    fun generate(sounds: List<Sound>, layerCount: Int, keep: List<Layer> = emptyList()): Scene {
        val keptIds = keep.mapTo(mutableSetOf()) { it.soundId }
        val downloaded = sounds.filter { it.isDownloaded && it.id !in keptIds }
        val beds = downloaded.filter { it.kind == SoundKind.BED }.shuffled(random)
        val events = downloaded.filter { it.kind == SoundKind.EVENT }.shuffled(random)

        val free = (layerCount - keep.size).coerceAtLeast(0)
        val keptBeds = keep.count { it is Layer.Bed }
        val wantedBeds = maxOf(if (layerCount >= TWO_BEDS_FROM) 2 else 1, keptBeds)
        val wantedEvents = minOf(MAX_EVENTS, layerCount - wantedBeds) - (keep.size - keptBeds)
        val pickedEvents = events.take(wantedEvents.coerceIn(0, free))
        val pickedBeds = beds.take(free - pickedEvents.size)
        val moreEvents = events.drop(pickedEvents.size).take(free - pickedBeds.size - pickedEvents.size)

        return Scene(
            (keep +
                pickedBeds.map { Layer.Bed(it.id, randomVolume()) } +
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
