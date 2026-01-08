package com.mustafakoceerr.justrelax.feature.splash.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.random.Random

private const val MIN_SIZE_DP = 20
private const val MAX_SIZE_ADD_DP = 40
private const val MIN_ALPHA = 0.1f
private const val MAX_ALPHA_ADD = 0.3f
private const val MIN_DURATION_MS = 10_000
private const val MAX_DURATION_ADD_MS = 15_000

@Composable
fun FloatingIcon(
    icon: ImageVector,
    screenHeight: Dp,
    screenWidth: Dp
) {
    val density = LocalDensity.current

    val size = remember { (MIN_SIZE_DP + Random.nextInt(MAX_SIZE_ADD_DP)).dp }
    val targetAlpha = remember { MIN_ALPHA + Random.nextFloat() * MAX_ALPHA_ADD }
    val duration = remember { MIN_DURATION_MS + Random.nextInt(MAX_DURATION_ADD_MS) }

    val startX = remember(screenWidth, density) { Random.nextFloat() * with(density) { screenWidth.toPx() } }
    val startY = remember(screenHeight, density) { Random.nextFloat() * with(density) { screenHeight.toPx() } }
    val endX = remember(screenWidth, density) { Random.nextFloat() * with(density) { screenWidth.toPx() } }
    val endY = remember(screenHeight, density) { Random.nextFloat() * with(density) { screenHeight.toPx() } }

    val infiniteTransition = rememberInfiniteTransition(label = "floating")

    val x by infiniteTransition.animateFloat(
        initialValue = startX,
        targetValue = endX,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "x"
    )

    val y by infiniteTransition.animateFloat(
        initialValue = startY,
        targetValue = endY,
        animationSpec = infiniteRepeatable(
            animation = tween((duration * 1.2).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration * 2, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                translationX = x
                translationY = y
                rotationZ = rotation
                alpha = targetAlpha
            },
        tint = MaterialTheme.colorScheme.primary
    )
}