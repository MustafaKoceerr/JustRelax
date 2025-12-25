package com.mustafakoceerr.justrelax.feature.splash.components

// --- IMPORT BURADA ---
import kotlin.random.Random
// --------------------

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun FloatingIcon(
    icon: ImageVector,
    screenHeight: Dp,
    screenWidth: Dp
) {
    // DÜZELTME: 'val random' değişkenine gerek yok.
    // Kotlin'in kendi 'Random' nesnesini direkt remember blokları içinde kullanıyoruz.

    // 1. Boyut: 20dp ile 60dp arası
    val size = remember { (20 + Random.nextInt(40)).dp }

    // 2. Opaklık: %10 ile %40 arası
    val alpha = remember { 0.1f + Random.nextFloat() * 0.3f }

    // 3. Süre: 10 saniye ile 25 saniye arası
    val duration = remember { 10000 + Random.nextInt(15000) }

    // 4. Başlangıç ve Bitiş Konumları
    val density = LocalDensity.current
    val screenWidthPx = with(density) { screenWidth.toPx() }
    val screenHeightPx = with(density) { screenHeight.toPx() }

    // Random.nextFloat() 0.0 ile 1.0 arası sayı üretir.
    val startX = remember { Random.nextFloat() * screenWidthPx }
    val startY = remember { Random.nextFloat() * screenHeightPx }

    val endX = remember { Random.nextFloat() * screenWidthPx }
    val endY = remember { Random.nextFloat() * screenHeightPx }

    // --- ANİMASYON ---
    val infiniteTransition = rememberInfiniteTransition(label = "floating")

    // X Ekseni Hareketi
    val x by infiniteTransition.animateFloat(
        initialValue = startX,
        targetValue = endX,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "x"
    )

    // Y Ekseni Hareketi
    val y by infiniteTransition.animateFloat(
        initialValue = startY,
        targetValue = endY,
        animationSpec = infiniteRepeatable(
            animation = tween((duration * 1.2).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y"
    )

    // Dönme Efekti
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration * 2, easing = LinearEasing)
        ),
        label = "rotation"
    )

    // --- ÇİZİM ---
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier
            .offset { IntOffset(x.toInt(), y.toInt()) }
            .size(size)
            .rotate(rotation)
            .alpha(alpha),
        tint = MaterialTheme.colorScheme.primary
    )
}