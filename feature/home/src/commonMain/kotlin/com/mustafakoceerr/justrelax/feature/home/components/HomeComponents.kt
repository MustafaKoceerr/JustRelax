package com.mustafakoceerr.justrelax.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.ui.components.SoundCard
import com.mustafakoceerr.justrelax.core.ui.components.VolumeSlider
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import com.mustafakoceerr.justrelax.feature.home.util.icon
import com.mustafakoceerr.justrelax.feature.home.util.titleRes
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTabRow(
    categories: List<SoundCategory>,
    selectedCategory: SoundCategory,
    onCategorySelected: (SoundCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    PrimaryScrollableTabRow(
        selectedTabIndex = categories.indexOf(selectedCategory),
        modifier = modifier,
        edgePadding = 16.dp, // İlk eleman kenara yapışmasın

        // 1. ŞEFFAFLIK: Arkadaki gradyanı ezmemesi için Transparent yapıyoruz
        containerColor = Color.Transparent,

        // 2. ÇİZGİYİ YOK ET: Varsayılan alt çizgiyi (Indicator) boş bir lambda ile siliyoruz
        indicator = {},
        divider = {}
    ) {
        categories.forEach { category ->
            val isSelected = category == selectedCategory

            // Renk Mantığı (BottomBar ile tutarlı)
            // Seçiliyse: PrimaryContainer (Renkli), Değilse: Transparent
            val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

            Tab(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                // Ripple efektini istersen kapatabilirsin ama varsayılan kalması iyidir.
            ) {
                // 3. HAP TASARIMI (Custom Layout)
                // İkon ve Metni bir Column içine alıp, arka planı buna veriyoruz.
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 4.dp) // Tablar arası boşluk
                        .clip(CircleShape) // Veya RoundedCornerShape(16.dp)
                        .background(backgroundColor) // Seçim rengi burada!
                        .padding(horizontal = 16.dp, vertical = 12.dp) // Hapın iç dolgunluğu
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(category.titleRes),
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun SoundCardGrid(
    sounds: List<Sound>,
    activeSounds: Map<String, Float>,
    downloadingSoundIds: Set<String>, // YENİ: Şu an inenlerin listesi
    onSoundClick: (Sound) -> Unit,
    onVolumeChange: (String, Float) -> Unit,
    contentPadding: PaddingValues
) {

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 110.dp),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = sounds,
            key = { it.id }
        ) { sound ->
            val isPlaying = activeSounds.containsKey(sound.id)
            val volume = activeSounds[sound.id] ?: 0.5f

            // Bu ses şu an indirilenler listesinde mi?
            val isDownloading = downloadingSoundIds.contains(sound.id)

            SoundCard(
                sound = sound,
                isPlaying = isPlaying,
                isDownloading = isDownloading,
                volume = volume,
                onCardClick = { onSoundClick(sound) },
                onVolumeChange = { newVol -> onVolumeChange(sound.id, newVol) }
            )
        }

    }
}





@Composable
@Preview
fun SliderPreview() {
    JustRelaxTheme {
        VolumeSlider(0.3f, {})
    }
}




