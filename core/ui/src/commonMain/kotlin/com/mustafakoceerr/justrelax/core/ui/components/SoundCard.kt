package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.extensions.displayName
import com.mustafakoceerr.justrelax.core.ui.extensions.rememberDebouncedOnClick
import com.mustafakoceerr.justrelax.core.ui.generated.resources.Res
import com.mustafakoceerr.justrelax.core.ui.generated.resources.sound_action_download
import org.jetbrains.compose.resources.stringResource

@Composable
fun SoundCard(
    sound: Sound,
    isPlaying: Boolean,
    isDownloading: Boolean,
    volume: Float,
    onCardClick: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // --- ANİMASYON TANIMLARI ---
    // Renk geçişleri için "animateColorAsState" kullanıyoruz.
    // label parametresi Android Studio Inspector için faydalıdır.
    // Orijinal onCardClick'i, debounce korumasıyla sarmalıyoruz.
    val debouncedOnCardClick = rememberDebouncedOnClick(debounceMs = 500, onClick = onCardClick)

    val cardContainerColor by animateColorAsState(
        targetValue = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = tween(durationMillis = 300),
        label = "CardContainerColor"
    )

    val iconCircleColor by animateColorAsState(
        targetValue = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = tween(durationMillis = 300),
        label = "IconCircleColor"
    )

    val iconTintColor by animateColorAsState(
        targetValue = if (isPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 300),
        label = "IconTintColor"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- Kartın Gövdesi ---
        Surface(
            onClick = debouncedOnCardClick,
            modifier = Modifier.aspectRatio(1f),
            shape = MaterialTheme.shapes.medium,
            color = cardContainerColor // Animasyonlu renk
        ) {
            Box(contentAlignment = Alignment.Center) {

                // --- İkon Arka Planı (Daire) ---
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .alpha(if (sound.isDownloaded) 1f else 0.3f),
                    shape = CircleShape,
                    color = iconCircleColor, // Animasyonlu renk
                    contentColor = iconTintColor // Animasyonlu renk
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AsyncImage(
                            model = sound.iconUrl,
                            contentDescription = sound.displayName(),
                            modifier = Modifier.size(24.dp),
                            contentScale = ContentScale.Fit,
                            colorFilter = ColorFilter.tint(iconTintColor) // Animasyonlu renk
                        )
                    }
                }

                // --- Durum Katmanı (İndirme İkonu veya Spinner) ---
                if (!sound.isDownloaded) {
                    // İndirme durumu değişirken de ufak bir fade animasyonu hoş olur
                    androidx.compose.animation.AnimatedVisibility(
                        visible = true, // Her zaman görünür ama içerik değişebilir
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 3.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CloudDownload,
                                    contentDescription = stringResource(Res.string.sound_action_download),
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Alt Bilgi (Slider veya İsim) ---
        // AnimatedContent: İsim giderken Slider gelir (veya tam tersi).
        // Bu geçiş "gecikme" hissini tamamen yok eder.
        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = {
                // Yaylanma efekti (Spring) ile açılış
                (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                        scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
                    .togetherWith(fadeOut(animationSpec = tween(90)))
            },
            label = "BottomContent"
        ) { playing ->
            if (playing) {
                VolumeSlider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
            } else {
                Text(
                    text = sound.displayName(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = if (sound.isDownloaded) 1f else 0.5f
                    )
                )
            }
        }
    }
}