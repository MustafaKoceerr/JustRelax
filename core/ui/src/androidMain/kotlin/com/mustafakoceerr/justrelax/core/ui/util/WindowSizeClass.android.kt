package com.mustafakoceerr.justrelax.core.ui.util

import android.app.Activity
import android.content.Context
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import android.content.ContextWrapper
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
actual fun rememberWindowSizeClass(): WindowWidthSize {
    val activity = LocalContext.current.findActivity()
    val windowSize = calculateWindowSizeClass(activity)

    return when (windowSize.widthSizeClass) {
        WindowWidthSizeClass.Compact -> WindowWidthSize.COMPACT
        WindowWidthSizeClass.Medium -> WindowWidthSize.MEDIUM
        WindowWidthSizeClass.Expanded -> WindowWidthSize.EXPANDED
        else -> WindowWidthSize.COMPACT
    }
}

private fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Activity not found in context.")
}