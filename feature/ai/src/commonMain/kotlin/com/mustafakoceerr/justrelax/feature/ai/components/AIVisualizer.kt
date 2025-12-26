package com.mustafakoceerr.justrelax.feature.ai.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AIVisualizer(
    isThinking: Boolean,
    modifier: Modifier = Modifier,
    // İYİLEŞTİRME 1: Boyutu dışarıdan alınabilir hale getirdik.
    size: Dp = 240.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing_transition")

    // İYİLEŞTİRME 2: Animasyon özelliklerini 'isThinking' değiştiğinde
    // yeniden oluşturulacak şekilde 'remember' içine aldık.
    val scaleAnimationSpec = remember(isThinking) {
        infiniteRepeatable<Float>(
            animation = tween(if (isThinking) 600 else 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    }
    val alphaAnimationSpec = remember(isThinking) {
        infiniteRepeatable<Float>(
            animation = tween(if (isThinking) 600 else 2000),
            repeatMode = RepeatMode.Reverse
        )
    }

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isThinking) 1.2f else 1.05f,
        animationSpec = scaleAnimationSpec,
        label = "scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = alphaAnimationSpec,
        label = "alpha"
    )

    val glowColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier.size(size), // Parametreden gelen boyutu kullandık
        contentAlignment = Alignment.Center
    ) {
        // Dış Halka (Glow)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
                .background(glowColor.copy(alpha = 0.3f), CircleShape)
        )

        // İç Çekirdek
        Surface(
            modifier = Modifier.size(size / 2), // Boyutu orantılı olarak küçülttük
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = GeminiStarIcon,
                    contentDescription = null,
                    modifier = Modifier.size(size / 5), // İkonu da orantılı yaptık
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
