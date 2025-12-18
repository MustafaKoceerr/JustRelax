package com.mustafakoceerr.justrelax.core.ui.controller

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult

class GlobalSnackbarController {
    // State'i burada tutuyoruz. MainScreen bunu dinleyecek.
    val hostState = SnackbarHostState()

    // Her yerden çağırabileceğimiz fonksiyon
    suspend fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short
    ): SnackbarResult {
        return hostState.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = duration
        )
    }
}