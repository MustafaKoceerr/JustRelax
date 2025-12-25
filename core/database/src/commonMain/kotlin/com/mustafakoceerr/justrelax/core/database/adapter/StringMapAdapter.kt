package com.mustafakoceerr.justrelax.core.database.adapter

import app.cash.sqldelight.ColumnAdapter
import kotlinx.serialization.json.Json

/**
 * SQLDelight için Map<String, String> <-> JSON String dönüşümü yapan adaptör.
 */
internal class StringMapAdapter : ColumnAdapter<Map<String, String>, String> {
    override fun decode(databaseValue: String): Map<String, String> {
        return if (databaseValue.isBlank()) {
            emptyMap()
        } else {
            try {
                Json.decodeFromString(databaseValue)
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }

    override fun encode(value: Map<String, String>): String {
        return Json.encodeToString(value)
    }
}