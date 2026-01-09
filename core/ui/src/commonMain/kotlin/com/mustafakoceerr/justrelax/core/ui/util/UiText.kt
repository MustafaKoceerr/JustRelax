package com.mustafakoceerr.justrelax.core.ui.util

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * A wrapper class to decouple string resources from the Android Context or Composable scope.
 * Allows ViewModels to pass text data (either raw strings or resource IDs) to the UI.
 */
sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    data class Resource(
        val resId: StringResource,
        val formatArgs: List<Any> = emptyList()
    ) : UiText

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is Resource -> stringResource(resId, *formatArgs.toTypedArray())
        }
    }

    suspend fun resolve(): String {
        return when (this) {
            is DynamicString -> value
            is Resource -> {
                if (formatArgs.isEmpty()) getString(resId)
                else getString(resId, *formatArgs.toTypedArray())
            }
        }
    }
}