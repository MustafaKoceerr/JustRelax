package com.mustafakoceerr.justrelax.core.ui.components

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
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ActiveSoundsBar(
    activeIcons: List<String>,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onStopAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    //Bu bar dikkat çekici olmallı bu yüzden PrimaryContainer kullanıyoruz.
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp), // Standart mini player yüksekliği.
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,

        // üzerindeki ikonların rengi (Koyu kahve)
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,

        // Hafif bir gölge (elevation) verelim ki içerikten ayrılsın.
        shadowElevation = 4.dp
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Sol taraf: çalan ikonlar.
            // weight 1 f veriyoruz ki butonların kalan tüm alanı kapsasın
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp), // İkonlar arası boşluk,
                contentPadding = PaddingValues(end = 16.dp) // butonlara yapışmasın
            ) {
                items(activeIcons.size) { index ->
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        // İkonlar kendi içinde bir tık daha kkoyu/farklı dursun
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            AsyncImage(
                                model = activeIcons[index],
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
                            )
                        }
                    }
                }
            }

            // Sağ taraf : Kotnrol butonları
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Play/ pause butonu
                IconButton(onClick = onPlayPauseClick) {
                    Icon(
                        // Duruma göre icon değişir.
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(32.dp) // Buton biraz büyük olsun
                    )
                }


                // 2. Hepsini Durdur (Kapat) Butonu
                // Bu biraz daha "tehlikeli" veya "kapatma" işlemi olduğu için
                // farklı bir stil verebiliriz ama şimdilik IconButton yeterli.
                IconButton(onClick = onStopAllClick) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close all sounds",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

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
        ActiveSoundsBar(
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