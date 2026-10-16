package com.mustafakoceerr.justrelax.core.data.repository

import com.mustafakoceerr.justrelax.core.common.Resource
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import kotlinx.coroutines.flow.StateFlow

/**
 * The scene the user is listening to: the single source of truth for what plays. Screens edit it
 * here; the player follows it. Whether it is playing or paused is the player's state, not the scene's.
 */
interface SceneRepository {
    val scene: StateFlow<Scene>

    /** Adds a downloaded sound at its last volume, or removes it if it is already in the scene. */
    suspend fun toggle(soundId: String): Resource<Unit>

    fun setVolume(soundId: String, volume: Float)

    fun setFrequency(soundId: String, frequency: EventFrequency)

    /** Replaces the scene with a mix (sound id to volume); sounds that are not downloaded are skipped. */
    suspend fun replaceWith(volumes: Map<String, Float>)

    fun clear()
}
