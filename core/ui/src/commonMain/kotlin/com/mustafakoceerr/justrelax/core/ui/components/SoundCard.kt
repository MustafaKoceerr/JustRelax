package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.ui.extensions.displayName
import com.mustafakoceerr.justrelax.core.ui.extensions.rememberThrottledOnClick
import com.mustafakoceerr.justrelax.core.ui.generated.resources.Res
import com.mustafakoceerr.justrelax.core.ui.generated.resources.sound_action_download
import org.jetbrains.compose.resources.stringResource

// --- SABİTLER ---
private const val ANIM_COLOR_DURATION = 300
private const val ANIM_CONTENT_ENTER_DURATION = 220
private const val ANIM_CONTENT_EXIT_DURATION = 90
private const val ANIM_CONTENT_DELAY = 90
@Composable
fun SoundCard(
    sound: Sound, // Sound modelinin proje içinde tanımlı olduğunu varsayıyorum
    isPlaying: Boolean,
    isDownloading: Boolean,
    volume: Float,
    onCardClick: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Debounce işlemi (yardımcı fonksiyonun var olduğunu varsayıyoruz)
    // Eğer yoksa basit bir clickable kullanılabilir ama bu daha güvenli.
    val debouncedClick = rememberThrottledOnClick(throttleMs = 500, onClick = onCardClick)

    // Renk Animasyonları
    val cardContainerColor by animateColorAsState(
        targetValue = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = tween(ANIM_COLOR_DURATION),
        label = "CardContainerColor"
    )

    val iconColors = IconColors(
        circleColor = animateColorAsState(
            targetValue = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
            animationSpec = tween(ANIM_COLOR_DURATION),
            label = "IconCircleColor"
        ).value,
        tintColor = animateColorAsState(
            targetValue = if (isPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            animationSpec = tween(ANIM_COLOR_DURATION),
            label = "IconTintColor"
        ).value
    )

    Column(
        modifier = modifier
            // Erişilebilirlik: Bu bir karttır ve durumu vardır.
            .semantics {
                role = Role.Button
                stateDescription = if (isPlaying) "Playing" else "Stopped"
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp) // PDF Kuralı: 8dp ritim
    ) {
        // 1. KART GÖVDESİ (Resim + Overlay)
        CardSurface(
            onClick = debouncedClick,
            containerColor = cardContainerColor,
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Ses İkonu
                SoundIcon(
                    iconUrl = sound.iconUrl,
                    displayName = sound.displayName(),
                    isDownloaded = sound.isDownloaded,
                    colors = iconColors
                )

                // İndirme Durumu (Varsa üzerine biner)
                DownloadOverlay(
                    isDownloaded = sound.isDownloaded,
                    isDownloading = isDownloading
                )
            }
        }

        // 2. ALT KONTROLLER (Slider <-> İsim)
        BottomControls(
            isPlaying = isPlaying,
            soundName = sound.displayName(),
            isDownloaded = sound.isDownloaded,
            volume = volume,
            onVolumeChange = onVolumeChange
        )
    }
}

// --- ALT BİLEŞENLER (Private & Stateless) ---

// Veri Sınıfı: Çoklu renk parametresi geçmek yerine wrapper kullandık.
private data class IconColors(val circleColor: Color, val tintColor: Color)

@Composable
private fun CardSurface(
    onClick: () -> Unit,
    containerColor: Color,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1f),
        shape = MaterialTheme.shapes.medium, // PDF: 12dp-16dp arası shape
        color = containerColor,
        content = content
    )
}

@Composable
private fun SoundIcon(
    iconUrl: String,
    displayName: String,
    isDownloaded: Boolean,
    colors: IconColors
) {
    Surface(
        modifier = Modifier
            .size(48.dp) // PDF: Dokunma/Görsel hedef en az 48dp
            .alpha(if (isDownloaded) 1f else 0.3f),
        shape = CircleShape,
        color = colors.circleColor,
        contentColor = colors.tintColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            AsyncImage(
                model = iconUrl,
                contentDescription = null, // Üst container zaten açıklıyor
                modifier = Modifier.size(24.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(colors.tintColor)
            )
        }
    }
}

@Composable
private fun DownloadOverlay(
    isDownloaded: Boolean,
    isDownloading: Boolean
) {
    if (!isDownloaded) {
        AnimatedVisibility(
            visible = true,
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

@Composable
private fun BottomControls(
    isPlaying: Boolean,
    soundName: String,
    isDownloaded: Boolean,
    volume: Float,
    onVolumeChange: (Float) -> Unit
) {
    AnimatedContent(
        targetState = isPlaying,
        transitionSpec = {
            (fadeIn(animationSpec = tween(ANIM_CONTENT_ENTER_DURATION, delayMillis = ANIM_CONTENT_DELAY)) +
                    scaleIn(initialScale = 0.92f, animationSpec = tween(ANIM_CONTENT_ENTER_DURATION, delayMillis = ANIM_CONTENT_DELAY)))
                .togetherWith(fadeOut(animationSpec = tween(ANIM_CONTENT_EXIT_DURATION)))
        },
        label = "BottomContent"
    ) { playing ->
        if (playing) {
            // Slider'ın ayrı bir component olduğunu varsayıyorum.
            // Eğer yoksa Material3 Slider kullanılmalı.
            VolumeSlider(
                value = volume,
                onValueChange = onVolumeChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
        } else {
            Text(
                text = soundName,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = if (isDownloaded) 1f else 0.5f
                )
            )
        }
    }
}