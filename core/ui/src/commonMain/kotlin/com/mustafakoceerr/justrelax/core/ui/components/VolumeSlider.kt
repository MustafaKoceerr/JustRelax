package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * SoundCard'a özel Slider bileşeni.
 * İnce ayar (height 24.dp) yapıldığı için ayrı bir fonksiyon olarak burada durması doğrudur.
 */
@Composable
fun VolumeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = 0f..1f, // AudioMixer 0.0-1.0 arası çalışır
        modifier = modifier.height(24.dp),
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary,
            inactiveTickColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}