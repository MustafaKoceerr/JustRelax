package com.mustafakoceerr.justrelax.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Shows a one-off [message] kept in UI state, then reports it as shown so the ViewModel can clear it.
 * Because the message lives in state until consumed, it survives configuration changes.
 */
@Composable
fun UserMessageEffect(
    message: UiText?,
    onMessageShown: () -> Unit,
    show: suspend (String) -> Unit,
) {
    LaunchedEffect(message) {
        if (message == null) return@LaunchedEffect
        show(message.resolve())
        onMessageShown()
    }
}
