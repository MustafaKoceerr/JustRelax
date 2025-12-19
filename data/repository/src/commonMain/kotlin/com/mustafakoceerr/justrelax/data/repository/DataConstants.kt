package com.mustafakoceerr.justrelax.data.repository

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Bu modülün iç (internal) sabitleri ve DataStore anahtarları.
 */
internal object DataConstants {
    // DataStore için kullanılacak dosya adı.
    const val SETTINGS_DATASTORE_NAME = "justrelax_settings"

    // DataStore Anahtarları (Keys)
    val KEY_APP_THEME = stringPreferencesKey("app_theme")
    val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
    val KEY_STARTER_PACK_INSTALLED = booleanPreferencesKey("starter_pack_installed")

    val KEY_LAST_SOUND_SYNC = longPreferencesKey("last_sound_sync_timestamp")

    // YENİ: Ses dosyalarının saklanacağı alt klasörün adı.
    const val SOUNDS_DIRECTORY_NAME = "sounds"
}