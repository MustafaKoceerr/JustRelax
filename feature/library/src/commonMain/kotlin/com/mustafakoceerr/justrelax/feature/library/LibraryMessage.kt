package com.mustafakoceerr.justrelax.feature.library

/** A one-off message for the library screen; each platform turns it into its own text. */
sealed interface LibraryMessage {
    data object DownloadFailed : LibraryMessage

    /** Shown as it is. */
    data class Text(val value: String) : LibraryMessage
}
