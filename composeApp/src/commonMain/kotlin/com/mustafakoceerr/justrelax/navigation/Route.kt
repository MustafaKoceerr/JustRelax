package com.mustafakoceerr.justrelax.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/** Top-level destinations of the app. Serializable so the back stack survives process death. */
@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Splash : Route

    /** Bottom-bar shell hosting the [MainTab]s. */
    @Serializable
    data object Main : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object Credits : Route

    @Serializable
    data object MySounds : Route

    @Serializable
    data object Recorder : Route

    /** Cuts a new sound out of a file on the device; [origin] is set for a sound found online. */
    @Serializable
    data class SoundEditor(
        val sourcePath: String,
        val suggestedName: String?,
        val origin: EditorOrigin? = null,
    ) : Route

    @Serializable
    data object Freesound : Route

    companion object {
        /** Needed to save/restore the back stack on non-JVM targets (iOS), where reflection is unavailable. */
        val serializersModule = SerializersModule {
            polymorphic(NavKey::class) {
                subclass(Splash::class)
                subclass(Main::class)
                subclass(Settings::class)
                subclass(Credits::class)
                subclass(MySounds::class)
                subclass(Recorder::class)
                subclass(SoundEditor::class)
                subclass(Freesound::class)
            }
        }
    }
}

/** Opens [route] on top of the stack, unless it is already the current destination. */
fun <T : NavKey> MutableList<T>.navigate(route: T) {
    if (lastOrNull() != route) add(route)
}

/** Clears the history and makes [route] the only destination (e.g. after the splash). */
fun <T : NavKey> MutableList<T>.resetTo(route: T) {
    clear()
    add(route)
}

/** Pops the current destination. The root is never removed; returns false in that case. */
fun MutableList<out NavKey>.goBack(): Boolean {
    if (size <= 1) return false
    removeAt(lastIndex)
    return true
}
