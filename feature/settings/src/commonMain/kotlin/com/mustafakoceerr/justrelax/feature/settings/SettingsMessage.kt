package com.mustafakoceerr.justrelax.feature.settings

/** A one-off message for Settings; each platform turns it into its own text. */
sealed interface SettingsMessage {
    data object LibraryDownloaded : SettingsMessage

    /** Shown as it is. */
    data class Text(val value: String) : SettingsMessage
}
