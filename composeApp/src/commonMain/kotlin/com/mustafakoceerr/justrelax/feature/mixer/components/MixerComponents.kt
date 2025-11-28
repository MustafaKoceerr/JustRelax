package com.mustafakoceerr.justrelax.feature.mixer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import com.mustafakoceerr.justrelax.feature.home.components.SoundCard
import com.mustafakoceerr.justrelax.feature.mixer.MixerViewModel
import kotlinx.coroutines.selects.select
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.collections.emptyList


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MixerTopBar() {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                text = "Mixer",
                style = MaterialTheme.typography.titleLarge
            )
        }
    )
}

@Preview
@Composable
fun MixerTopBarPreview() {
    JustRelaxTheme {
        MixerTopBar()
    }
}

@Composable
fun MixNumberChip(
    number: Int,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {

    FilterChip(
        selected = isSelected,
        onClick = onSelected,
        label = {
            Text(
                text = number.toString(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth() // yazının daire içinde olmasını garantiler
            )
        },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            // Seçiliyken: Arka plan PrimaryContainer (Somon), Yazı onPrimaryContainer (Kahve)
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,

            // Seçili Değilken: Arka plan SurfaceContainer (Hafif Gri/Bej), Yazı onSurface
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),

        // kenarlık: seçiliyken primary renkli ince bir çizgi, değilken şeffaf.
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
            borderWidth = 1.dp
        ),

        // İKONU KALDIRMA: Varsayılan tik işaretini null yaparak kaldırıyoruz
        leadingIcon = null,

        modifier = modifier.size(50.dp) // 48-50dp ideal dokunma alanıdır
    )

}

@Preview
@Composable
fun MixNumberChipPreview() {
    JustRelaxTheme {
        Column {
            MixNumberChip(
                5, true, {}
            )

            MixNumberChip(
                5, false, {}
            )
        }
    }
}


@Composable
fun MixCountSelector(
    selectedCount: Int,
    onCountSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Başlık
        Text(
            text = "Kaç ses ile mix yapmak istiyorsun?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
        )

        // Liste
        LazyRow(
            // içerik kenarlara yapışmasın diyee padding
            contentPadding = PaddingValues(horizontal = 16.dp),
            // elemanlar arası boşluk (8 dp grid kuralı)
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 2'den 82e kadar olan sayılar.
            items(7) { index ->
                val number = index + 2

                MixNumberChip(
                    number = number,
                    isSelected = number == selectedCount,
                    onSelected = { onCountSelected(number) }
                )
            }
        }
    }
}

@Preview
@Composable
fun MixCountSelectorPreview() {
    JustRelaxTheme {
        MixCountSelector(
            6,
            {}
        )
    }
}

@Composable
fun CreateMixButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    //  M3 filled button ( en yüksek vurgu)
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth() // Genişliği doldursun ( padding'i dışarıdan vereceğiz.)
            .height(50.dp), // Timer butonlarıyla tutarlı yükseklik ( kibar ama basılabilir.)

        // Renkler: Tema varsayılanı zaten primary'dir ama emin olmak için:
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        // Şekil: Hafif yuvarlatılmış (Varsayılan Stadium/Tam yuvarlak yerine)
        // Timer butonlarına benzesin diye RoundedCornerShape(12.dp) veya varsayılan bırakabilirsin.
        // M3 varsayılanı (Stadium) genelde ana aksiyonlar için iyidir.
        shape = MaterialTheme.shapes.extraLarge
    ){
        Icon(
            imageVector = Icons.Rounded.Refresh,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )

        // Araya boşluk
        Spacer(modifier = Modifier.width(8.dp))

        // Metin
        Text(
            text = "Mix oluştur", // veya "Create Mix"
            style = MaterialTheme.typography.titleMedium // Okunaklı, kalın font
        )
    }
}

@Preview
@Composable
fun CreateMixButtonPreview(){
    JustRelaxTheme {
        CreateMixButton({})
    }
}



@Composable
fun SaveMixButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
){
    // Secondary Action (İkincil Eylem) -> FilledTonalButton
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        shape = MaterialTheme.shapes.extraLarge // Yuvarlak hatlar
    ){
        Icon(
            imageVector = Icons.Rounded.Save,
            contentDescription = null,
            modifier= Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Mix'i Kaydet", // Save Mix
            style = MaterialTheme.typography.titleMedium
        )
    }
}



@Preview
@Composable
fun SaveMixButtonPreview(){
    JustRelaxTheme {
        SaveMixButton({})
    }
}


@Composable
fun MixerScreen(){
    // --- STATE (Durumlar) ---
    var selectedCount by remember { mutableIntStateOf(4) } // Seçilen sayı (chip)

    // Oluşturulan Mix Listesi (Şimdilik Boolean listesi ile simüle ediyoruz)
    // Boş liste = Henüz mix yok.
    // Dolu liste = Mix var, kartlar gösterilecek.
    var generatedMix by remember { mutableStateOf<List<Boolean>>(emptyList()) }

    Scaffold(
        topBar = {MixerTopBar()}
    ) {paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // 1. Üst Kısım (Sabit)

            // A) Sayı seçici
            MixCountSelector(
                selectedCount = selectedCount,
                onCountSelected = {selectedCount =it},
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // B) oluştur butonu
            Box(modifier = Modifier.padding(horizontal = 16.dp)){
                CreateMixButton(
                    onClick = {
                        // MOCK LOGIC: Seçilen sayı kadar "true" (aktif) kart oluştur
                        generatedMix = List(selectedCount) { true }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // -- 2. Alt kısım (GRID) --
            if (generatedMix.isNotEmpty()){
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 110.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f) // Kalan tüm alanı kapla
                ){
                    // A) Kartlar
                    items(generatedMix.size){
                        // SENİN HAZIR SOUND CARD'IN
                        // isPlaying = true gönderiyoruz çünkü mix oluştuğunda çalmaya başlar.
//                        SoundCard(
//                            isPlaying = true,
//                            modifier = Modifier.animateItem() // Animasyonlu giriş.
//                        )
                    }

                    // B) Kaydet butonu (Grid'in en sonuna eklenir.
                    // span parametresi ile tüm satırı kapla diyoruz.
                    item(span = { GridItemSpan(maxLineSpan)}){
                        Box(
                            modifier = Modifier
                                .padding(top = 24.dp, bottom = 32.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ){
                            // Butonu çok geniş yapmayalım, %60 genişlik yeterli
                            SaveMixButton(
                                onClick = {/* Kaydetme işlemi */},
                                modifier = Modifier.fillMaxWidth(0.6f)
                            )
                        }
                    }
                }
            }else{
                // --- BOŞ DURUM (EMPTY STATE) ---
                // Henüz mix oluşturulmadıysa
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ){
                    Text(
                        text = "Bir sayı seç ve karıştır!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}





@Preview
@Composable
fun MixerScreenPreview(){
    JustRelaxTheme {
        MixerScreen()
    }
}



