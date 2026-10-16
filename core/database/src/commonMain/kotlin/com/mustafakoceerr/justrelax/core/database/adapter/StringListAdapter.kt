package com.mustafakoceerr.justrelax.core.database.adapter

import app.cash.sqldelight.ColumnAdapter
import kotlinx.serialization.json.Json

internal class StringListAdapter : ColumnAdapter<List<String>, String> {
    override fun decode(databaseValue: String): List<String> =
        if (databaseValue.isBlank()) emptyList() else Json.decodeFromString(databaseValue)

    override fun encode(value: List<String>): String = Json.encodeToString(value)
}
