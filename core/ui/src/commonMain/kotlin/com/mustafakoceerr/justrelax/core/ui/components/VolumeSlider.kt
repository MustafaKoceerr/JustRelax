package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * SoundCard'a özel, dikey alanı az kaplayan Slider bileşeni.
 * Not: 24dp yükseklik görseldir, ancak Slider'ın dokunmatik alanı
 * Android tarafından otomatik olarak genişletilmeye çalışılır.
 */
@Composable
fun VolumeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Erişilebilirlik için yüzdelik hesaplama
    val percentage = (value * 100).roundToInt()

    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = 0f..1f,
        modifier = modifier
            .height(24.dp) // Görsel yükseklik (Tasarım kararı)
            .semantics {
                // Ekran okuyucu: "Ses Seviyesi, Yüzde 50" diyecek.
                contentDescription = "Ses Seviyesi"
                stateDescription = "%$percentage"
            },
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary,
            // inactiveTickColor yerine inactiveTrackColor daha doğru olur
            inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.Transparent
        ),
        // Slider'ın ucundaki top (Thumb) boyutunu değiştirmek istersek buraya thumb = {} yazabiliriz.
        // Ancak standart M3 thumb'ı (20dp) bu yükseklikte en ergonomik olandır.
    )
}