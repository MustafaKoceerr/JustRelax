package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.util.formatDurationVerbose
import com.mustafakoceerr.justrelax.feature.timer.util.rememberClockTimeFormatter
import com.mustafakoceerr.justrelax.feature.timer.util.toFormattedTime
import kotlin.time.Instant

/** Seconds left at which the ring turns to the warning color. */
private const val WARNING_SECONDS = 5

/**
 * The countdown: a ring that empties smoothly, the total length, the time left and the time it
 * ends. Each tick recomposes only the time left; the ring moves in the draw phase.
 */
@Composable
internal fun TimerCountdown(
    totalSeconds: Long,
    remainingSeconds: Long,
    isPaused: Boolean,
    endsAt: Instant?,
    dimensions: TimerDimensions,
    modifier: Modifier = Modifier,
) {
    TimerRing(
        totalSeconds = totalSeconds,
        remainingSeconds = remainingSeconds,
        isPaused = isPaused,
        strokeWidth = dimensions.ringStroke,
        modifier = modifier,
    ) {
        val caption = MaterialTheme.typography.titleMedium.copy(
            fontSize = dimensions.captionSize,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TotalLabel(totalSeconds = totalSeconds, style = caption)
        RemainingTime(
            remainingSeconds = remainingSeconds,
            isPaused = isPaused,
            style = MaterialTheme.typography.displayLarge.copy(
                // Three groups of digits need more room inside the ring than two.
                fontSize = if (remainingSeconds >= 3600) dimensions.countdownSize * 0.8f else dimensions.countdownSize,
                color = MaterialTheme.colorScheme.onSurface,
            ),
        )
        EndTimeLabel(endsAt = endsAt, style = caption)
    }
}

@Composable
private fun TimerRing(
    totalSeconds: Long,
    remainingSeconds: Long,
    isPaused: Boolean,
    strokeWidth: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val progress = remember { Animatable(ringProgress(remainingSeconds, totalSeconds)) }
    LaunchedEffect(remainingSeconds, totalSeconds, isPaused) {
        if (isPaused) {
            progress.stop()
        } else {
            // Ticks come once a second: glide to where the ring will be at the next one.
            progress.animateTo(ringProgress(remainingSeconds - 1, totalSeconds), tween(1_000, easing = LinearEasing))
        }
    }

    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val ringColor = animateColorAsState(
        targetValue = if (remainingSeconds in 1..WARNING_SECONDS) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        animationSpec = tween(500),
        label = "RingColor",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .drawBehind {
                val stroke = strokeWidth.toPx()
                val topLeft = Offset(stroke / 2, stroke / 2)
                val arcSize = Size(size.width - stroke, size.height - stroke)
                drawArc(trackColor, 0f, 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(stroke))
                drawArc(
                    color = ringColor.value,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.value,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            content()
        }
    }
}

@Composable
private fun TotalLabel(totalSeconds: Long, style: TextStyle) {
    Text(text = formatDurationVerbose(totalSeconds), style = style)
}

/** Blinks while paused, as on a kitchen timer. */
@Composable
private fun RemainingTime(remainingSeconds: Long, isPaused: Boolean, style: TextStyle) {
    val text = remainingSeconds.toFormattedTime()
    if (isPaused) {
        val blink = rememberInfiniteTransition(label = "PausedBlink").animateFloat(
            initialValue = 1f,
            targetValue = 0.25f,
            animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
            label = "PausedAlpha",
        )
        Text(text = text, style = style, maxLines = 1, modifier = Modifier.graphicsLayer { alpha = blink.value })
    } else {
        Text(text = text, style = style, maxLines = 1)
    }
}

/** Hidden, but still taking its space, while there is no end time (paused). */
@Composable
private fun EndTimeLabel(endsAt: Instant?, style: TextStyle) {
    val format = rememberClockTimeFormatter()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.graphicsLayer { alpha = if (endsAt == null) 0f else 1f },
    ) {
        Icon(
            imageVector = Icons.Rounded.Notifications,
            contentDescription = null,
            tint = style.color,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = endsAt?.let(format).orEmpty(), style = style)
    }
}
