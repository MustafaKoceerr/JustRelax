package com.mustafakoceerr.justrelax.feature.saved.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedMixesTopBar(
    onFilterClick: () -> Unit, // Filtre ikonuna basılınca ne olacak? (Bottom Sheet açılacak)
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        // Renkler: Mixer ekranıyla tutarlı olsun diye PrimaryContainer kullanabiliriz
        // Veya daha sade olsun dersen Background/Surface de olur.
        // Senin "MixerTopBar"da PrimaryContainer kullandığını hatırlıyorum, tutarlılık için aynısını yapalım.
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        title = {
            Text(
                text = "Kayıtlı Mixler",
                style = MaterialTheme.typography.titleLarge
            )
        },
        actions = {
            // Sağ taraftaki Filtre/sırala ikonu
            IconButton(onClick = onFilterClick) {
                Icon(
                    // FilterList veya sort ikonu uygun
                    imageVector = Icons.AutoMirrored.Rounded.Sort,
                    contentDescription = "Sırala ve Filtrele"
                )
            }
        }
    )
}

@Preview
@Composable
fun SavedMixesTopBarPreview(
) {
    JustRelaxTheme {
        SavedMixesTopBar({})
    }
}

@Composable
fun SavedMixInfo(
    title: String,
    soundCount: Int,
    date: String, // Örn: 2 gün önce
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Başlık
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Alt bilgi (metadata)
        // "8 Ses • 2 gün önce" formatında
        Text(
            text = "$soundCount Ses • $date",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SavedMixInfoPreview() {
    JustRelaxTheme {
        Surface {
            SavedMixInfo(
                title = "Deep Forest Mix",
                soundCount = 6,
                date = "2 gün önce",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun SavedMixIcons(
    icons: List<ImageVector>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(icons.size) { index ->
            Icon(
                imageVector = icons[index],
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

        }
    }
}


@Preview(showBackground = true)
@Composable
fun SavedMixIconsPreview() {
    JustRelaxTheme {
        Surface {
            SavedMixIcons(
                icons = listOf(
                    Icons.Default.Favorite,
                    Icons.Default.Star,
                    Icons.Default.Headset,
                    Icons.Default.MusicNote,
                    Icons.Default.Favorite,
                    Icons.Default.Star,
                    Icons.Default.Headset,
                    Icons.Default.MusicNote,
                    Icons.Default.Favorite,
                    Icons.Default.Star,
                    Icons.Default.Headset,
                    Icons.Default.MusicNote,
                    Icons.Default.Favorite,
                    Icons.Default.Star,
                    Icons.Default.Headset,
                    Icons.Default.MusicNote
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}


@Composable
fun SavedMixPlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // FilledTonal: Hem belirgin hem de kartın üzerinde çok bağırmayan en iyi stil.
    FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            // Aktifse Primary (Renkli), Pasifse Secondary (Gri/Bej)
            containerColor = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            contentColor = if (isPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            contentDescription = if (isPlaying) "Duraklat" else "Oynat",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SavedMixPlayButtonPreview() {
    JustRelaxTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            // Pause ikonlu (oynuyor durumu)
            SavedMixPlayButton(
                isPlaying = true,
                onClick = {}
            )

            // Play ikonlu (durmuş durumu)
            SavedMixPlayButton(
                isPlaying = false,
                onClick = {}
            )
        }
    }
}

@Composable
fun SavedMixMenu(
    onRenameClick:() -> Unit,
    onShareClick: ()-> Unit,
    onDeleteClick:()-> Unit,
    modifier: Modifier = Modifier
){
    // Menünün açık/kapalı durumunu burada tutuyoruz
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier){
        // 1. üç nokta ikonu
        IconButton(
            onClick = {expanded = true},
            modifier = Modifier.size(32.dp) // Biraz küçük olabilir, ikincil işlem
        ){
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Seçenekler",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 2. Açılır menü
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {expanded = false },
            // Menü arkalanı
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(12.dp)
        ){
            DropdownMenuItem(
                text = {Text("Yeniden adlandır")},
                onClick = {
                    expanded = false
                    onRenameClick()
                },
                leadingIcon = { Icon(Icons.Rounded.Edit, null)}
            )

            // Share
            DropdownMenuItem(
                text = {Text("Paylaş")},
                onClick = {
                    expanded = false
                    onShareClick()
                },
                leadingIcon = {Icon(Icons.Rounded.Share,null)}
            )

            // Delete (Kritik işlem olduğu için kırmızı yapabiliriz opsiyonel olarak)
            DropdownMenuItem(
                text = {Text("Sil", color = MaterialTheme.colorScheme.error)},
                onClick = {
                    expanded = false
                    onDeleteClick()
                },
                leadingIcon = {Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error )}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SavedMixMenuPreview() {
    JustRelaxTheme {
        Surface(
            modifier = Modifier.padding(16.dp)
        ) {
            SavedMixMenu(
                onRenameClick = {},
                onShareClick = {},
                onDeleteClick = {}
            )
        }
    }
}


@Composable
fun SavedMixCard(
    title: String,
    date: String,
    soundCount: Int,
    icons: List<ImageVector>,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onRenameClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // --- ANIMASYONLU ARKA PLAN ---
    // Kartın rengi duruma göre yumuşakça değişsin
    val animatedContainerColor by animateColorAsState(
        targetValue = if(isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = tween (durationMillis = 500), // 500 ms yumuşak geçiş
        label = "CardBackgroundColor"
    )

    Card(
        modifier= modifier
            .fillMaxWidth()
        // Tıklanabilir yapıyoruz (Detay sayfasına gitmek istersen diye, şimdilik boş)
            .clickable{onPlayClick()},
        shape = RoundedCornerShape(16.dp), // Modern, yumuşak köşeler.
        colors = CardDefaults.cardColors(
            containerColor = animatedContainerColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPlaying) 4.dp else 1.dp // Çalan kart hafif yükselsin.
        )
    ) {
        // Box kullanıyoruz çünkü "Menu" butonunu sağ üst köşeye (Absolute Position) koyacağız.
        Box(modifier = Modifier.fillMaxWidth()){
            // --- ANA İÇERİK (ROW) ---
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SOL TARAFTAKİ BLOK (BİLGİ + İKONLAR)
                // weight(1f) veriyoruz ki Play butonuna kadar olan tüm alanı kaplasın.
                Column(modifier = Modifier.weight(1f)) {
                    // 1. Atom: Bilgi
                    SavedMixInfo(
                        title= title,
                        soundCount= soundCount,
                        date= date
                    )

                    Spacer(modifier = Modifier.height(12.dp)) // Araya ferahlık

                    // 2. Atom: İkonlar
                    SavedMixIcons(icons = icons)
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Sağ taraftaki blok (play buttonu)
                //  3. Atom: play buttonu
                SavedMixPlayButton(
                    isPlaying = isPlaying,
                    onClick = onPlayClick
                )
            }
            // --- MENÜ (SAĞ ÜST KÖŞE) ---
            // 4. Atom: Menü
            // Row akışından bağımsız, sağ üst köşeye sabitliyoruz.
            SavedMixMenu(
                onRenameClick = onRenameClick,
                onShareClick = onShareClick,
                onDeleteClick = onDeleteClick,
                modifier= Modifier.align(Alignment.TopEnd)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SavedMixCardPreview() {
    JustRelaxTheme {
        Surface {
            SavedMixCard(
                title = "Deep Focus Mix",
                date = "3 gün önce",
                soundCount = 6,
                icons = listOf(
                    Icons.Default.Headset,
                    Icons.Default.Star,
                    Icons.Default.Favorite,
                    Icons.Default.MusicNote
                ),
                isPlaying = false,
                onPlayClick = {},
                onRenameClick = {},
                onShareClick = {},
                onDeleteClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SavedMixCardComparePreview() {
    JustRelaxTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            // Çalmayan
            SavedMixCard(
                title = "Soft Rain Mix",
                date = "1 gün önce",
                soundCount = 4,
                icons = listOf(
                    Icons.Default.Cloud,
                    Icons.Default.BubbleChart,
                    Icons.Default.WaterDrop
                ),
                isPlaying = false,
                onPlayClick = {},
                onRenameClick = {},
                onShareClick = {},
                onDeleteClick = {}
            )

            // Çalan kart
            SavedMixCard(
                title = "Forest Wind Mix",
                date = "5 saat önce",
                soundCount = 7,
                icons = listOf(
                    Icons.Default.Eco,
                    Icons.Default.Air,
                    Icons.Default.Spa,
                    Icons.Default.Nature
                ),
                isPlaying = true,
                onPlayClick = {},
                onRenameClick = {},
                onShareClick = {},
                onDeleteClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SavedMixCardLongTitlePreview() {
    JustRelaxTheme {
        Surface {
            SavedMixCard(
                title = "Extremely Long Ambient Mix Name for Stress Relief and Deep Relaxation",
                date = "2 hafta önce",
                soundCount = 12,
                icons = listOf(
                    Icons.Default.Headset,
                    Icons.Default.MusicNote,
                    Icons.Default.Favorite
                ),
                isPlaying = false,
                onPlayClick = {},
                onRenameClick = {},
                onShareClick = {},
                onDeleteClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}


