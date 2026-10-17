package com.mustafakoceerr.justrelax.core.data

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

internal object DataConstants {
    /** The user's choices (theme, language); the only data Android backs up. */
    const val SETTINGS_DATASTORE_NAME = "justrelax_settings.preferences_pb"

    /** The app's own state on this device (downloads, syncs); never backed up. */
    const val STATE_DATASTORE_NAME = "justrelax_state.preferences_pb"

    /** Koin qualifiers for the two files. */
    const val SETTINGS = "settings"
    const val STATE = "state"

    val KEY_APP_THEME = stringPreferencesKey("app_theme")
    val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
    val KEY_STARTER_PACK_INSTALLED = booleanPreferencesKey("starter_pack_installed")
    val KEY_LAST_SOUND_SYNC = longPreferencesKey("last_sound_sync_timestamp")
    val KEY_CATALOG_VERSION = intPreferencesKey("catalog_version")
}
