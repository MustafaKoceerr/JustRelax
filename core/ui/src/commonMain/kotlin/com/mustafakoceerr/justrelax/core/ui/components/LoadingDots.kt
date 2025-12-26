package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview

// Sabitler: Animasyonun "hissi"ni buradan yönetiriz.
private const val ANIMATION_CYCLE_DURATION = 600
private const val ANIMATION_DELAY_PER_DOT = 100

@Composable
fun LoadingDots(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary, // Varsayılan renk atandı
    dotSize: Dp = 8.dp, // PDF Grid: 8dp
    travelDistance: Dp = 6.dp,
    spaceBetween: Dp = 4.dp // PDF Grid: 4dp
) {
    // Performans: Px dönüşümünü döngü dışında bir kez yapıyoruz.
    val density = LocalDensity.current
    val travelDistancePx = remember(travelDistance, density) {
        with(density) { travelDistance.toPx() }
    }

    // 3 nokta için animasyon durumu
    val dots = listOf(
        remember { Animatable(0f) },
        remember { Animatable(0f) },
        remember { Animatable(0f) }
    )

    dots.forEachIndexed { index, animatable ->
        LaunchedEffect(animatable) {
            // Enterprise Fix: 'delay' yerine 'initialStartOffset' kullanmak daha senkrondur
            // ancak Animatable ile kullanımı karmaşıktır.
            // Burada basitlik ve okunabilirlik adına delay mantığını koruyoruz
            // ama frame kaymasını önlemek için StartOffset kullanmayı tercih ederiz.
            // Aşağıdaki yapı "StartOffset" mantığının manual halidir.

            delay(index * ANIMATION_DELAY_PER_DOT.toLong())

            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = ANIMATION_CYCLE_DURATION
                        0.0f at 0 using LinearOutSlowInEasing
                        1.0f at (ANIMATION_CYCLE_DURATION / 4) using LinearOutSlowInEasing // Zıplama tepe noktası
                        0.0f at (ANIMATION_CYCLE_DURATION / 2) using LinearOutSlowInEasing // İniş
                        0.0f at ANIMATION_CYCLE_DURATION using LinearOutSlowInEasing // Bekleme
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    // Container
    Row(
        // Erişilebilirlik: Ekran okuyucu buranın "Yükleniyor" olduğunu bilecek.
        modifier = modifier.semantics {
            contentDescription = "Yükleniyor"
        },
        horizontalArrangement = Arrangement.spacedBy(spaceBetween),
        verticalAlignment = Alignment.CenterVertically
    ) {
        dots.forEach { animatable ->
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .graphicsLayer {
                        // Burada toPx hesaplaması yapmıyoruz, yukarıdakini kullanıyoruz.
                        translationY = -animatable.value * travelDistancePx
                    }
                    .background(
                        color = color,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Preview
@Composable
private fun LoadingDotsPreview() {
    MaterialTheme {
        Box(modifier = Modifier.background(Color.White).size(100.dp), contentAlignment = Alignment.Center) {
            LoadingDots(color = Color.Black)
        }
    }
}
