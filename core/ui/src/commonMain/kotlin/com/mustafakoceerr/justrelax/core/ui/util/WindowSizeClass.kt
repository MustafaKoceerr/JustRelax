package com.mustafakoceerr.justrelax.core.ui.util

import androidx.compose.runtime.Composable

enum class WindowWidthSize {
    COMPACT, MEDIUM, EXPANDED
}

@Composable
expect fun rememberWindowSizeClass(): WindowWidthSize