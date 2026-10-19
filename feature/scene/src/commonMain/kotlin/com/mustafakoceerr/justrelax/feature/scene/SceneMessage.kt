package com.mustafakoceerr.justrelax.feature.scene

/** A one-off message about the scene; each platform turns it into its own text. */
sealed interface SceneMessage {
    data class Saved(val name: String) : SceneMessage
    data object EmptyName : SceneMessage
    data object NameTaken : SceneMessage
    data object NothingPlaying : SceneMessage
    data object SaveFailed : SceneMessage
}
