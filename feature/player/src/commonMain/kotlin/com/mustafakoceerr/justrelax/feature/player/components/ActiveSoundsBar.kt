package com.mustafakoceerr.justrelax.feature.player.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mustafakoceerr.justrelax.core.ui.extensions.rememberDebouncedOnClick
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Bu ana kapsayıcıdır. Animasyonu ve görünürlüğü yönetir.
 * Scaffold'un bottomBar parametresine veya Box içinde en alta koyulabilir.
 */
@Composable
fun PlayerBottomBar(
    isVisible: Boolean, // State'den gelecek (activeSounds.isNotEmpty())
    activeIcons: List<String>,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onStopAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        // Aşağıdan yukarıya kayarak gel + Fade In
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight }, // Kendi boyu kadar aşağıdan başla
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeIn(),
        // Aşağıya kayarak kaybol + Fade Out
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight }, // Kendi boyu kadar aşağı git
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeOut()
    ) {
        ActiveSoundsBarContent(
            activeIcons = activeIcons,
            isPlaying = isPlaying,
            onPlayPauseClick = onPlayPauseClick,
            onStopAllClick = onStopAllClick
        )
    }
}

@Composable
private fun ActiveSoundsBarContent(
    activeIcons: List<String>,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onStopAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Play/Pause butonu için 1 saniyelik korumalı bir lambda oluşturuyoruz.
    val debouncedOnPlayPauseClick = rememberDebouncedOnClick(
        debounceMs = 500L, // 1 saniye
        onClick = onPlayPauseClick
    )

    // Not: onStopAllClick de ağır bir işlem olduğu için ona da debounce eklenebilir.
    // Şimdilik sadece Play/Pause için ekliyoruz.
    val debouncedOnStopAllClick = rememberDebouncedOnClick(
        debounceMs = 1000L,
        onClick = onStopAllClick
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp) // Yükseklik biraz artırıldı, padding için pay
            .padding(horizontal = 16.dp, vertical = 8.dp), // Kenarlardan boşluk (Floating hissi için)
        shape = RoundedCornerShape(24.dp), // Daha yuvarlak hatlar
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shadowElevation = 8.dp, // Gölge artırıldı
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // SOL: Aktif İkonlar
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy((-8).dp), // İkonlar hafif iç içe geçsin (Avatar group gibi)
                contentPadding = PaddingValues(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(activeIcons) { iconUrl ->
                    SoundIconItem(iconUrl)
                }
            }

            // SAĞ: Kontroller
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play/Pause
                IconButton(
                    onClick = debouncedOnPlayPauseClick,
                    modifier = Modifier.size(48.dp) // Dokunma alanı geniş
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Stop All
                IconButton(
                    onClick = debouncedOnStopAllClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close all",
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SoundIconItem(url: String) {
    Surface(
        modifier = Modifier
            .size(36.dp)
            .padding(2.dp), // Border efekti için boşluk
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer) // Arka planla karışmasın diye sınır
    ) {
        Box(contentAlignment = Alignment.Center) {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun ActiveSoundsBarPreview() {
    MaterialTheme {
        PlayerBottomBar(
            isVisible = true,
            activeIcons = listOf(
                "https://example.com/icon1.png",
                "https://example.com/icon2.png",
                "https://example.com/icon3.png",
                "https://example.com/icon4.png",
                "https://example.com/icon5.png",
                "https://example.com/icon6.png"
            ),
            isPlaying = true,
            onPlayPauseClick = {},
            onStopAllClick = {},
            modifier = Modifier
        )
    }
}