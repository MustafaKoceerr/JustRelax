package com.mustafakoceerr.justrelax.feature.saved.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Stop
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mustafakoceerr.justrelax.ui.theme.JustRelaxTheme
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
    icons: List<String>, // URL Listesi
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onRenameClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onPlayClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainer,
            contentColor = if (isPlaying) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // SOL TARA (Metinler ve İkonlar)
            // Weight veriyoruz ki sağdaki buton sıkışmasın
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "$soundCount Ses • $date",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // --- DÜZELTME: LAZY ROW ---
                // --- GÜNCELLEME BURADA ---
                LazyRow(
                    // Boyut büyüdüğü için binme payını -8'den -12'ye çektik, daha tok durur.
                    horizontalArrangement = Arrangement.spacedBy((-12).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(icons.size) { index ->
                        Surface(
                            shape = CircleShape,
                            border = BorderStroke(
                                width = 2.dp,
                                color = if (isPlaying) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceContainer
                            ),
                            // Container Boyutu: 28dp -> 36dp yaptık
                            modifier = Modifier.size(36.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                AsyncImage(
                                    model = icons[index],
                                    contentDescription = null,
                                    // İkon Boyutu: 18dp -> 24dp yaptık
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(2.dp),
                                    contentScale = ContentScale.Fit,
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }
            }

            // SAĞ TARAF (Play Butonu)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    onClick = onPlayClick,
                    shape = CircleShape,
                    color = if (isPlaying) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                            contentDescription = "Play",
                            tint = if (isPlaying) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}