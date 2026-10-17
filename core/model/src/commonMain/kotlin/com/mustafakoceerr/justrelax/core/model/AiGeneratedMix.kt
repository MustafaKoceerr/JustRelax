package com.mustafakoceerr.justrelax.core.model

/** A mix the AI put together from the user's request. */
data class AiGeneratedMix(
    val name: String,
    val description: String,
    val sounds: Map<LocalizedSound, Float>
)
