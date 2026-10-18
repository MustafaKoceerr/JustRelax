package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.testing.TestTags
import com.mustafakoceerr.justrelax.core.ui.generated.resources.Res
import com.mustafakoceerr.justrelax.core.ui.generated.resources.volume_level
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
fun VolumeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val percentage = (value * 100).roundToInt()
    val label = stringResource(Res.string.volume_level)

    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = 0f..1f,
        modifier = modifier
            .height(24.dp)
            .testTag(TestTags.VOLUME_SLIDER)
            .semantics {
                contentDescription = label
                stateDescription = "$percentage%"
            },
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary,
            inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.Transparent
        )
    )
}