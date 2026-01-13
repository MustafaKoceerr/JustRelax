package com.mustafakoceerr.justrelax.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalWindowWidthSize = staticCompositionLocalOf { WindowWidthSize.COMPACT }

@Composable
actual fun rememberWindowSizeClass(): WindowWidthSize {
    return LocalWindowWidthSize.current
}