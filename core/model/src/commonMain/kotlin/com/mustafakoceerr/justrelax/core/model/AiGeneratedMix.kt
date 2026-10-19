package com.mustafakoceerr.justrelax.core.model

import com.mustafakoceerr.justrelax.core.model.soundscape.Scene
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/** A scene the AI put together from the user's request, with its sounds named in the app language. */
@OptIn(ExperimentalObjCName::class)
data class AiGeneratedMix(
    val name: String,
    /** `mixDescription` in Swift: every Objective-C object already has a `description`. */
    @ObjCName("mixDescription")
    val description: String,
    val scene: Scene,
    val sounds: List<LocalizedSound>,
)
