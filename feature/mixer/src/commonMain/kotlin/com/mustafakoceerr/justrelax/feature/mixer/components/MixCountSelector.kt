package com.mustafakoceerr.justrelax.feature.mixer.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import justrelax.feature.mixer.generated.resources.Res
import justrelax.feature.mixer.generated.resources.mix_count_selector_title
import org.jetbrains.compose.resources.stringResource

/**
 * Kullanıcının mikse kaç ses ekleyeceğini seçmesini sağlayan bileşen.
 * @param selectedCount O an seçili olan sayı.
 * @param onCountSelected Bir sayı seçildiğinde tetiklenen fonksiyon.
 * @param availableCounts Seçenek olarak sunulacak sayıların listesi.
 */
@Composable
fun MixCountSelector(
    selectedCount: Int,
    onCountSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    availableCounts: List<Int> = (2..7).toList()
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.mix_count_selector_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp)
        )

        // Değişiklik: Row yerine LazyRow kullanıldı.
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            // Değişiklik: forEach yerine LazyListScope'un 'items' fonksiyonu kullanıldı.
            items(items = availableCounts) { number ->
                MixNumberChip(
                    number = number,
                    isSelected = number == selectedCount,
                    onSelected = { onCountSelected(number) }
                )
            }
        }
    }
}

/**
 * MixCountSelector içinde kullanılan, seçili durumu olan dairesel sayı çipi.
 * Bu bileşen dışarıdan erişime kapalıdır (private).
 */
@Composable
private fun MixNumberChip(
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
            )
        },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true, // Çip her zaman aktif olduğu için 'true'
            selected = isSelected, // Seçili durumunu buradan alıyor
            borderColor = Color.Transparent,
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            borderWidth = 1.dp
        ),
        modifier = modifier.size(48.dp)
    )
}