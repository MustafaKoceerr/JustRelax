package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun JustRelaxBackground(
    modifier: Modifier = Modifier, // 1. Best Practice: Modifier parametresi ekledik
    content: @Composable () -> Unit
) {
    val colorTop = MaterialTheme.colorScheme.surfaceContainer
    val colorBottom = MaterialTheme.colorScheme.background

    // 2. Performans: Renkler değişmediği sürece Brush'ı tekrar oluşturmuyoruz.
    val backgroundBrush = remember(colorTop, colorBottom) {
        Brush.verticalGradient(
            colors = listOf(
                colorTop,
                colorBottom
            )
        )
    }

    Box(
        modifier = modifier // Dışarıdan gelen modifier'ı en başa ekliyoruz (zincirleme sırası önemli)
            .fillMaxSize()
            .background(brush = backgroundBrush)
    ) {
        content()
    }
}

@Preview
@Composable
private fun JustRelaxBackgroundPreview() {
    MaterialTheme {
        JustRelaxBackground {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Background Test Content")
            }
        }
    }
}