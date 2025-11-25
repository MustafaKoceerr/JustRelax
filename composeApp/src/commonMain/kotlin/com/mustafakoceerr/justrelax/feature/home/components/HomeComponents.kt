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
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.VolumeUp
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.lifecycle.rememberScreenLifecycleOwner
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import org.jetbrains.compose.ui.tooling.preview.Preview


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar() {
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
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings"
                )
            }
        }
    )
}

//@Composable
//fun LeadingIconTab(
//    selected: Boolean,
//    onClick: () -> Unit,
//    text: @Composable () -> Unit,
//    icon: @Composable () -> Unit,
//    modifier: Modifier = Modifier,
//    enabled: Boolean = true,
//    selectedContentColor: Color = LocalContentColor.current,
//    unselectedContentColor: Color = selectedContentColor,
//    interactionSource: MutableInteractionSource? = null
//): Unit

@Composable
fun HomeTabRow(
    modifier: Modifier = Modifier
) {
    // Hangi tab'ın seçili olduğunu tutan hafıza
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Başlıklar ve İkonlar Listesi
    val tabTitles = listOf(
        "Video",
        "Photos",
        "Audio",
        "Tab",
        "Other",
        "Video",
        "Photos",
        "Audio",
        "Tab",
        "Other"
    )
    val tabIcons = listOf(
        Icons.Outlined.Videocam,
        Icons.Outlined.Image,
        Icons.Outlined.Audiotrack,
        Icons.Outlined.Star,
        Icons.Outlined.Settings, Icons.Outlined.Videocam,
        Icons.Outlined.Image,
        Icons.Outlined.Audiotrack,
        Icons.Outlined.Star,
        Icons.Outlined.Settings
    )
    PrimaryScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.primary,
    ) {
        tabTitles.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { selectedTabIndex == index },
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                icon = {
                    Icon(
                        imageVector = tabIcons[index],
                        contentDescription = title
                    )
                },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

    }

}

@Composable
fun SoundCard(
    isPlaying: Boolean,
    modifier: Modifier = Modifier // Dışarıdan müdahale için
) {
    Column(
        modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.aspectRatio(1f),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Ikon sabit kalır 32 dp. ortada şık görünür. ikonlar sabit dp verilirler.
                Surface(
                    modifier = Modifier.size(48.dp),// İkonun kapsayıcısı SABİT
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Videocam,
                            modifier = Modifier.size(24.dp), // İkon boyutu standart 24dp
                            contentDescription = null
                        )
                    }
                }
            }

        }

        if (isPlaying){
            VolumeSlider(
                value = 0.5f, // Şimdilik sabit, sonra viewModel'den gelecek
                onValueChange = { /* Logic gelince dolacak */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp) // Kartın kenarlarına yapışmasın
            )
        }else{
            Text(
                text = "Video",
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
fun SoundCardContent(){
    val soundStates = listOf(
        // Satır 1: True, True, False
        true, true, false,

        // Satır 2: False, True, True
        false, true, true,

        // Satır 3: False, False, False
        false, false, false,

        // Satır 4: True, True, True
        true, true, true,

        // Satır 5: True, False, False
        true, false, false,

        // Satır 6: Karışık
        false, true, false
    )

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 110.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ){

        items(soundStates) { isPlaying ->

            SoundCard(
                isPlaying = isPlaying, // LİSTEDEN GELEN DEĞER (true/false)
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
@Composable
@Preview
fun SoundCardContentPreview() {
    JustRelaxTheme {
        SoundCardContent()
    }
}

@Composable
@Preview
fun SoundCardPreview() {
    JustRelaxTheme {
        SoundCard(false)
    }
}

@Composable
@Preview
fun HomeTabRowPreview() {
    JustRelaxTheme {
        HomeTabRow()
    }
}

@Preview
@Composable
fun HomeTopBarPreview() {
    JustRelaxTheme {
        HomeTopBar()
    }
}

@Composable
fun FullHomeScreen() {
    Scaffold(
        topBar = {
            HomeTopBar()
        }
    ) { }
}


