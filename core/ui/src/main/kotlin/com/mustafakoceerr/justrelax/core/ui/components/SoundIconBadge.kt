package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.model.SoundIcon
import com.mustafakoceerr.justrelax.core.ui.icon.imageVector

/**
 * A sound's icon in a small ringed circle, for overlapping rows of a mix's sounds. [borderColor]
 * should match what is behind the row, so neighbours look cut out of each other.
 */
@Composable
fun SoundIconBadge(
    icon: SoundIcon,
    borderColor: Color,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(36.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(2.dp, borderColor),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon.imageVector, contentDescription = null, modifier = Modifier.size(20.dp), tint = tint)
        }
    }
}
