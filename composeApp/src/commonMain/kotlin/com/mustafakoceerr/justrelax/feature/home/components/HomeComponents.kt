package com.mustafakoceerr.justrelax.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.sound.domain.model.Sound
import com.mustafakoceerr.justrelax.core.sound.domain.model.SoundCategory
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
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
        categories.forEach { category->
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
                    /*
                       Icon(
                           imageVector = if(isSelected) Icons.Rounded.Star else Icons.Outlined.StarBorder,
                           contentDescription = null
                       )
                        */
                },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

    }

}

@Composable
fun SoundCard(
    sound: Sound,
    isPlaying: Boolean,
    volume: Float,
    onCardClick:()-> Unit,
    onVolumeChange:(Float)-> Unit,
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
            modifier = Modifier.aspectRatio(1f),
            shape = MaterialTheme.shapes.medium,
            color = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Ikon sabit kalır 32 dp. ortada şık görünür. ikonlar sabit dp verilirler.
                Surface(
                    modifier = Modifier.size(48.dp),// İkonun kapsayıcısı SABİT
                    shape = CircleShape,
                    color = if (isPlaying) MaterialTheme.colorScheme.primary else
                        MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = if (isPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = sound.icon,
                            modifier = Modifier.size(24.dp), // İkon boyutu standart 24dp
                            contentDescription = null
                        )
                    }
                }
            }

        }

        if (isPlaying){
            VolumeSlider(
                value = volume,
                onValueChange = onVolumeChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp) // Kartın kenarlarına yapışmasın
            )
        }else{
            Text(
                text = stringResource(sound.nameRes), // Modelden gelen isim
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,             // Renk: onSurface (En net okunan siyah/koyu gri)
                modifier = Modifier.padding(horizontal = 4.dp) // Yanlardan hafif boşluk
                , color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


@Composable
fun SoundCardGrid(
    sounds: List<Sound>,
    activeSounds: Map<String, Float>,
    onSoundClick: (Sound) -> Unit,
    onVolumeChange: (String,Float) -> Unit,
    contentPadding: PaddingValues
){

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 110.dp),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ){
        items(
            items= sounds,
            key = {it.id}
        ){
            sound->
            val isPlaying = activeSounds.containsKey(sound.id)
            val volume = activeSounds[sound.id] ?: 0.5f

            SoundCard(
                sound = sound,
                isPlaying = isPlaying,
                volume = volume,
                onCardClick = {onSoundClick(sound)},
                onVolumeChange= {newVol -> onVolumeChange(sound.id, newVol)}
            )
        }

    }
}
@Composable
fun ActiveSoundsBar(
    activeIcons: List<ImageVector>,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onStopAllClick: () -> Unit,
    modifier: Modifier = Modifier
)
{

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
                items(activeIcons.size){index ->
                    Surface(modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        // İkonlar kendi içinde bir tık daha kkoyu/farklı dursun
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f ),
                        contentColor = MaterialTheme.colorScheme.onSurface
                 ) {
                        Box(contentAlignment = Alignment.Center){
                            Icon(
                                imageVector = activeIcons[index],
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Sağ taraf : Kotnrol butonları
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                // 1. Play/ pause butonu
                IconButton(onClick = onPlayPauseClick){
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
            IconButton(onClick = onStopAllClick){
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close all sounds",
                    modifier = Modifier.size(28.dp)
                )
            }}

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
                Icons.AutoMirrored.Rounded.VolumeUp,
                Icons.Rounded.MusicNote,
                Icons.Rounded.Headphones,
                Icons.AutoMirrored.Rounded.VolumeUp,
                Icons.Rounded.MusicNote,
                Icons.Rounded.Headphones,
                Icons.AutoMirrored.Rounded.VolumeUp,
                Icons.Rounded.MusicNote,
                Icons.Rounded.Headphones,
                Icons.AutoMirrored.Rounded.VolumeUp,
                Icons.Rounded.MusicNote,
                Icons.Rounded.Headphones
            ),
            isPlaying = true,
            onPlayPauseClick = { },
            onStopAllClick = { },
            modifier = Modifier
        )
    }
}

@Composable
fun VolumeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier){
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = 0f..1f,
        modifier = modifier
            .height(24.dp),
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary,
            inactiveTickColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}
@Composable
@Preview
fun SliderPreview() {
    JustRelaxTheme {
        VolumeSlider(0.3f, {})
    }
    }




