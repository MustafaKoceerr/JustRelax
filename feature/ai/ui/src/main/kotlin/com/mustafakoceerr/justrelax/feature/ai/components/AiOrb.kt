package com.mustafakoceerr.justrelax.feature.ai.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The AI's sparkle in a soft orb that breathes slowly while waiting and sends out rings while a
 * mix is being made. Animated values are read in the draw phase only.
 */
@Composable
fun AiOrb(isThinking: Boolean, modifier: Modifier = Modifier, size: Dp = 128.dp) {
    val transition = rememberInfiniteTransition(label = "AiOrb")
    val breath = transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(BREATH_MS, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Breath",
    )
    val primary = MaterialTheme.colorScheme.primary

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        AnimatedVisibility(visible = isThinking, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.fillMaxSize()) {
            ThinkingRings(color = primary)
        }
        Box(
            modifier = Modifier
                .size(size * 0.6f)
                .graphicsLayer {
                    scaleX = breath.value
                    scaleY = breath.value
                }
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surfaceContainerHigh),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = AiSparkleIcon, contentDescription = null, tint = primary, modifier = Modifier.size(size * 0.26f))
        }
    }
}

@Composable
private fun ThinkingRings(color: Color) {
    val transition = rememberInfiniteTransition(label = "ThinkingRings")
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(RING_MS, easing = LinearEasing)),
        label = "RingProgress",
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val maxRadius = size.minDimension / 2
        val minRadius = maxRadius * 0.3f
        repeat(RING_COUNT) { ring ->
            val p = (progress.value + ring / RING_COUNT.toFloat()) % 1f
            drawCircle(
                color = color,
                radius = minRadius + (maxRadius - minRadius) * p,
                alpha = (1f - p) * 0.35f,
                style = Stroke(width = 2.dp.toPx()),
            )
        }
    }
}

private const val BREATH_MS = 2_000
private const val RING_MS = 1_800
private const val RING_COUNT = 3
