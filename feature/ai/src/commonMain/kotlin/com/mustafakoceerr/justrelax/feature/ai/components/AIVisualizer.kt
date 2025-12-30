package com.mustafakoceerr.justrelax.feature.ai.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun AiVisualizer(
    isThinking: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary
) {
    // Sonsuz animasyon döngüsü tanımlayıcısı
    val infiniteTransition = rememberInfiniteTransition(label = "AiVisualizerLoop")

    // Animasyon hızı: Düşünüyorsa hızlı (600ms), beklemedeyse çok yavaş (2500ms)
    val duration = if (isThinking) 600 else 2500

    // Ölçek (Büyüme/Küçülme) Animasyonu
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isThinking) 1.2f else 1.05f, // Beklerken çok az hareket etsin
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ScaleAnimation"
    )

    // Opaklık (Yanıp Sönme) Animasyonu
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlphaAnimation"
    )

    Box(
        modifier = modifier, // Boyutlandırma dışarıdan (Parent) gelecek
        contentAlignment = Alignment.Center
    ) {
        // 1. Katman: Dış Halka (Glow Efekti)
        // graphicsLayer kullanımı sayesinde Recomposition tetiklenmez, GPU'da çalışır.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
                .clip(CircleShape)
                .background(primaryColor)
        )

        // 2. Katman: İç İkon (Sabit veya daha az hareketli)
        // İkonun kendisi net kalsın diye ayrı bir katmanda tutuyoruz.
        Box(
            modifier = Modifier
                .fillMaxSize(0.5f) // Dış halkanın yarısı kadar
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = GeminiIcon,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(0.6f),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}