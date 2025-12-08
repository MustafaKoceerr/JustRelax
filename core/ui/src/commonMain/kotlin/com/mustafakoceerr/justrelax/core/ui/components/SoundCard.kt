package com.mustafakoceerr.justrelax.core.ui.components

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

@Composable
fun SoundCard(
    sound: Sound,
    isPlaying: Boolean,
    isDownloading: Boolean, // YENİ: İndiriliyor mu?
    volume: Float,
    onCardClick: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier // Dışarıdan müdahale için
) {
    // Animasyon ekleyelim: Slider açılırken yumuşak geçiş olsun
    // val animateShape by animateDpAsState(...) yapılabilir ama şimdilik simple.

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            onClick = onCardClick,
            modifier = Modifier.aspectRatio(1f),
            shape = MaterialTheme.shapes.medium,
            color = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Ikon sabit kalır 32 dp. ortada şık görünür. ikonlar sabit dp verilirler.
                Surface(
                    modifier = Modifier.size(48.dp)
                        // İndirilmemişse veya indiriliyorsa biraz şeffaf yapalım (Alpha),// İkonun kapsayıcısı SABİT
                        .alpha(if (sound.isDownloaded) 1f else 0.3f), shape = CircleShape,
                    color = if (isPlaying) MaterialTheme.colorScheme.primary else
                        MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = if (isPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {

                        AsyncImage(
                            model = sound.iconUrl,
                            contentDescription = sound.name,
                            modifier = Modifier.size(24.dp),
                            contentScale = ContentScale.Fit,
                            colorFilter = ColorFilter.tint(
                                if (isPlaying) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
                // 2. DURUM KATMANI (İndirme İkonu veya Spinner)
                if (!sound.isDownloaded) {
                    if (isDownloading) {
                        // Dönüyor...
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        // İndir Butonu (Bulut)
                        // Arkasına ufak bir zemin atalım ki ikon karışmasın
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
                                contentDescription = "Download",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Slider veya İsim Alanı

        if (isPlaying) {
            VolumeSlider(
                value = volume,
                onValueChange = onVolumeChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp) // Kartın kenarlarına yapışmasın
            )
        } else {
            Text(
                text = sound.name, // Modelden gelen isim
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,             // Renk: onSurface (En net okunan siyah/koyu gri)
                modifier = Modifier.padding(horizontal = 4.dp), // Yanlardan hafif boşluk
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (sound.isDownloaded) 1f else 0.5f)
            )
        }
    }
}