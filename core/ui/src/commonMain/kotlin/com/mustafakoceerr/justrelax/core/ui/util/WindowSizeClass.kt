package com.mustafakoceerr.justrelax.core.ui.util

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass

enum class WindowWidthSize {
    COMPACT, MEDIUM, EXPANDED
}

/** The width class of the current window (Material breakpoints: 600dp and 840dp). */
@Composable
fun rememberWindowSizeClass(): WindowWidthSize {
    val sizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    return when {
        sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> WindowWidthSize.EXPANDED
        sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> WindowWidthSize.MEDIUM
        else -> WindowWidthSize.COMPACT
    }
}

/** True in a short window, such as a phone in landscape (under 480dp high). */
@Composable
fun rememberIsCompactHeight(): Boolean =
    !currentWindowAdaptiveInfoV2().windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)
