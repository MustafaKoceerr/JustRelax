package com.mustafakoceerr.justrelax.feature.mixer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.components.anims.LoadingDots
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

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

        val soundCounts = (2..8).toList() // [2, 3, 4, 5, 6, 7, 8]

        // Liste
        LazyRow(
            // içerik kenarlara yapışmasın diyee padding
            contentPadding = PaddingValues(horizontal = 16.dp),
            // elemanlar arası boşluk (8 dp grid kuralı)
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 2'den 82e kadar olan sayılar.
            items(soundCounts) { number ->
                MixNumberChip(
                    number = number,
                    isSelected = number == selectedCount,
                    onSelected = { onCountSelected(number) }
                )
            }
        }
    }
}
@Composable
fun CreateMixButton(
    onClick: () -> Unit,
    isLoading: Boolean, // YENİ PARAMETRE
    modifier: Modifier = Modifier
) {
    //  M3 filled button ( en yüksek vurgu)
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier
            .fillMaxWidth() // Genişliği doldursun ( padding'i dışarıdan vereceğiz.)
            .height(50.dp), // Timer butonlarıyla tutarlı yükseklik ( kibar ama basılabilir.)

        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            // Disabled olduğunda çok silik görünmemesi için container rengini ayarlayabiliriz
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = MaterialTheme.shapes.extraLarge
    ){
        if (isLoading){
            LoadingDots(
                color = MaterialTheme.colorScheme.onPrimary, // Beyaz/Kontrast renk
                dotSize = 8.dp,
                travelDistance = 6.dp
            )

        }else{
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
fun MixerTopBarPreview() {
    JustRelaxTheme {
        MixerTopBar()
    }
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

@Preview
@Composable
fun CreateMixButtonPreview(){
    JustRelaxTheme {
        CreateMixButton({}, false)
    }
}

@Preview
@Composable
fun SaveMixButtonPreview(){
    JustRelaxTheme {
        SaveMixButton({})
    }
}

