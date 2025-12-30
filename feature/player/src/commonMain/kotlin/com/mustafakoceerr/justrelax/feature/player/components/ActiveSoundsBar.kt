package com.mustafakoceerr.justrelax.feature.player.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.FavoriteBorder
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
import com.mustafakoceerr.justrelax.core.ui.extensions.rememberThrottledOnClick
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
    onSaveClick: () -> Unit,
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
            onStopAllClick = onStopAllClick,
            onSaveClick = onSaveClick // İçeriye pasla
        )
    }
}

@Composable
private fun ActiveSoundsBarContent(
    activeIcons: List<String>,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onStopAllClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Play/Pause butonu için 1 saniyelik korumalı bir lambda oluşturuyoruz.
    val debouncedOnPlayPauseClick = rememberThrottledOnClick(
        throttleMs = 500L, // 1 saniye
        onClick = onPlayPauseClick
    )
    val debouncedOnSaveClick = rememberThrottledOnClick(
        throttleMs = 1000L,
        onClick = onSaveClick
    )

    // Not: onStopAllClick de ağır bir işlem olduğu için ona da debounce eklenebilir.
    // Şimdilik sadece Play/Pause için ekliyoruz.
    val debouncedOnStopAllClick = rememberThrottledOnClick(
        throttleMs = 1000L,
        onClick = onStopAllClick
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp) // Yükseklik biraz artırıldı, padding için pay
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ), // Kenarlardan boşluk (Floating hissi için)
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
            // SOL: Aktif İkonlar (Weight azaltıldı ki butonlara yer kalsın)
            LazyRow(
                modifier = Modifier.weight(0.8f), // Biraz yer açtık
                horizontalArrangement = Arrangement.spacedBy((-8).dp),
                contentPadding = PaddingValues(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(activeIcons) { iconUrl ->
                    SoundIconItem(iconUrl)
                }
            }

            // SAĞ: Kontroller
            // Row içinde butonları grupluyoruz
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp), // Araları sıkı tutalım
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(0.6f, fill = false) // İhtiyacı kadar yer kaplasın
            ) {

                // 1. SAVE BUTONU (YENİ)
                // Kullanıcıya "Bunu beğendim" hissi vermek için Kalp ikonu
                IconButton(
                    onClick = debouncedOnSaveClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        // Dolu veya boş kalp durumu state'den de gelebilir ama şimdilik standart
                        imageVector = Icons.Rounded.FavoriteBorder,
                        contentDescription = "Save Mix",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary // Vurgulu renk
                    )
                }

                // 2. PLAY/PAUSE (En Büyük ve Ortada)
                IconButton(
                    onClick = debouncedOnPlayPauseClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        ) // Play butonunu arkasına renk atarak vurguladım
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                // 3. STOP ALL (En Sağda, biraz daha sönük)
                IconButton(
                    onClick = debouncedOnStopAllClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close all",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f) // Kapatma olduğu için hafif kırmızımsı olabilir veya gri
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
        border = BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.primaryContainer
        ) // Arka planla karışmasın diye sınır
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
