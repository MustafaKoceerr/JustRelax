package com.mustafakoceerr.justrelax.core.model

import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import kotlin.time.Instant

/** A scene the user saved under a name. */
data class SavedScene(
    val id: Long,
    val name: String,
    val createdAt: Instant,
    val scene: Scene,
)
