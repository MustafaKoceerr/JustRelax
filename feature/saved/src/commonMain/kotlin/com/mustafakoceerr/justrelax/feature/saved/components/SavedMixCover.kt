package com.mustafakoceerr.justrelax.feature.saved.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.model.SoundIcon
import com.mustafakoceerr.justrelax.core.ui.icon.imageVector
import kotlin.math.absoluteValue

/**
 * A saved mix's cover: up to four of its sound icons on a tone picked from its [name], so each
 * mix is told apart at a glance and keeps the same look.
 */
@Composable
fun SavedMixCover(icons: List<SoundIcon>, name: String, modifier: Modifier = Modifier, size: Dp = 64.dp) {
    val (container, content) = coverTone(name)
    val shown = icons.take(4)
    val iconSize = if (shown.size <= 1) size * 0.45f else size * 0.3f

    Surface(modifier = modifier.size(size), shape = RoundedCornerShape(size * 0.28f), color = container, contentColor = content) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(size * 0.12f)) {
            if (shown.size <= 1) {
                shown.firstOrNull()?.let { Icon(it.imageVector, contentDescription = null, modifier = Modifier.size(iconSize)) }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(size * 0.06f), horizontalAlignment = Alignment.CenterHorizontally) {
                    shown.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(size * 0.06f)) {
                            row.forEach { Icon(it.imageVector, contentDescription = null, modifier = Modifier.size(iconSize)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun coverTone(name: String): Pair<Color, Color> {
    val colors = MaterialTheme.colorScheme
    val tones = listOf(
        colors.primaryContainer to colors.onPrimaryContainer,
        colors.surfaceContainerHighest to colors.onSurfaceVariant,
        colors.secondaryContainer to colors.onSecondaryContainer,
        colors.primary to colors.onPrimary,
    )
    return tones[name.hashCode().absoluteValue % tones.size]
}

/** Three bars that bounce out of step, shown next to the mix that plays. */
@Composable
fun EqualizerBars(color: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "Equalizer")
    val bars = List(BAR_COUNT) { bar ->
        transition.animateFloat(
            initialValue = 0.25f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(BAR_DURATIONS[bar], easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(bar * 120),
            ),
            label = "Bar$bar",
        )
    }
    // The heights are read while drawing, so the bounce does not recompose anything.
    Canvas(modifier = modifier.size(14.dp)) {
        val gap = size.width * 0.15f
        val barWidth = (size.width - gap * (BAR_COUNT - 1)) / BAR_COUNT
        bars.forEachIndexed { index, height ->
            val barHeight = size.height * height.value
            drawRoundRect(
                color = color,
                topLeft = Offset(index * (barWidth + gap), size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2),
            )
        }
    }
}

private const val BAR_COUNT = 3
private val BAR_DURATIONS = listOf(420, 560, 360)
