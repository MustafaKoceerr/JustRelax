package com.mustafakoceerr.justrelax.core.model

import com.mustafakoceerr.justrelax.core.model.soundscape.Scene

/** A scene the AI put together from the user's request, with its sounds named in the app language. */
data class AiGeneratedMix(
    val name: String,
    val description: String,
    val scene: Scene,
    val sounds: List<LocalizedSound>,
)
