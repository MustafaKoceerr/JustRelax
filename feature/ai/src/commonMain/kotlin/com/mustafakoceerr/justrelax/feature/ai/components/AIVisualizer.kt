package com.mustafakoceerr.justrelax.feature.ai.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

// nefes alan parça
/**
 * Bu, ekranın kalbi. Lottie veya resim kullanmak yerine, Compose'un çizim yetenekleriyle (Canvas) "Nefes Alan" bir daire yapacağız. Hem çok performanslı hem de senin tema renklerine (Primary) otomatik uyum sağlayacak.
 */

@Composable
fun AIVisualizer(
    isThinking: Boolean, // AI düşünüyorsa animasyon hızlanabilir veya renk değişebilir.
    modifier: Modifier = Modifier
) {
    // Nefes Alma Animasyonu (Scale & Alpha)
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isThinking) 1.2f else 1.05f, // Düşünürken daha şişkin
        animationSpec = infiniteRepeatable(
            animation = tween(if (isThinking) 500 else 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isThinking) 500 else 2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    // --- BEST PRACTICE: Rengi Temadan Çekmek ---
    // Primary rengini alıyoruz. Dark modda otomatik değişecek.
    val glowColor = MaterialTheme.colorScheme.primary


    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // Dış halka (Glow efekti)
        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        }) {
            drawCircle(
                color = glowColor.copy(alpha = 0.3f),
                radius = size.minDimension / 2
            )
        }

        // İç çekirdek (sabit)
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center){
                Icon(
                    imageVector = GeminiStarIcon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Preview
@Composable
fun AIVisualizerPreview(){
    JustRelaxTheme {
        AIVisualizer(
            false
        )
    }
}