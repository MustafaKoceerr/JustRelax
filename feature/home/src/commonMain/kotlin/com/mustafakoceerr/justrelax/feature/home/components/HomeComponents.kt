package com.mustafakoceerr.justrelax.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.Modifier
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

// --- 1. Top Bar (Stateless) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    onSettingsClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                text = "Just Relax",
                style = MaterialTheme.typography.titleLarge
            )
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings"
                )
            }
        }
    )
}


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
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.primary,
        divider = {} // Çizgiyi kaldırdım daha temiz görünüm için
    ) {
        categories.forEach { category ->
            val isSelected = category == selectedCategory
            Tab(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                text = {
                    Text(
                        text = stringResource(category.titleRes), // Resource'dan çekiyoruz
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                icon = {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null
                    )
                },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
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




