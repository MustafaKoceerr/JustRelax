package com.mustafakoceerr.justrelax.feature.mixer.components

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun LoadingDots(
    color: Color,
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp, // Nokta boyutu
    travelDistance: Dp = 6.dp, // Ne kadar zıplayacak?
    spaceBetween: Dp = 4.dp // Noktalar arası boşluk
) {
    // 3 nokta için animasyon değerleri
    val dots = listOf(
        remember { Animatable(0f) },
        remember { Animatable(0f) },
        remember { Animatable(0f) }
    )

    // Animasyon döngüsü
    dots.forEachIndexed { index, animatable ->
        LaunchedEffect(animatable) {
            // Her nokta arasında ufak bir gecikme (stagger) olsun
            delay(index * 100L)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 600 // Toplam döngü hızı (500ms içine sığar)
                        0.0f at 0 using LinearOutSlowInEasing // Başlangıç
                        1.0f at 150 using LinearOutSlowInEasing // Zıplama tepe noktası
                        0.0f at 300 using LinearOutSlowInEasing // İniş
                        0.0f at 600 using LinearOutSlowInEasing // Bekleme
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    // Yan yana 3 nokta çiziyoruz
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spaceBetween),
        verticalAlignment = Alignment.CenterVertically
    ) {
        dots.forEach { animatable ->
            Box(
                modifier = Modifier
                    .size(dotSize)
                    // Y ekseninde yukarı kaydırma (Zıplama efekti)
                    .graphicsLayer {
                        translationY = -animatable.value * travelDistance.toPx()
                    }
                    .background(
                        color = color,
                        shape = CircleShape
                    )
            )
        }
    }
}