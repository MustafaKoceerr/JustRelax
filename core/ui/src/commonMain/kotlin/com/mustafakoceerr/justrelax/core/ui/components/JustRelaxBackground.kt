package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush

@Composable
fun JustRelaxBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colorTop = MaterialTheme.colorScheme.surfaceContainer
    val colorBottom = MaterialTheme.colorScheme.background

    val backgroundBrush = remember(colorTop, colorBottom) {
        Brush.verticalGradient(colors = listOf(colorTop, colorBottom))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = backgroundBrush)
    ) {
        content()
    }
}