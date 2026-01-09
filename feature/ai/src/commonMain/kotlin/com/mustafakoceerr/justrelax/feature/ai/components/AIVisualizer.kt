package com.mustafakoceerr.justrelax.feature.ai.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

@Composable
fun AiVisualizer(
    isThinking: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RippleLoop")

    val duration = if (isThinking) 4000 else 12000

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RippleProgress"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val maxRadius = size.minDimension / 2
            val waveCount = 3

            for (i in 0 until waveCount) {
                val offset = i / waveCount.toFloat()
                val waveProgress = (progress + offset) % 1f
                val currentRadius = maxRadius * waveProgress
                val baseAlpha = if (isThinking) 0.4f else 0.2f
                val currentAlpha = (1f - waveProgress) * baseAlpha

                drawCircle(
                    color = primaryColor,
                    radius = currentRadius,
                    alpha = currentAlpha
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize(0.4f)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = GeminiIcon,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(0.6f),
                tint = primaryColor
            )
        }
    }
}