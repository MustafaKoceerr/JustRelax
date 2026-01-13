package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mustafakoceerr.justrelax.core.ui.util.WindowWidthSize
import com.mustafakoceerr.justrelax.core.ui.util.rememberWindowSizeClass
import com.mustafakoceerr.justrelax.feature.timer.util.calculateEndTime
import com.mustafakoceerr.justrelax.feature.timer.util.formatDurationVerbose
import com.mustafakoceerr.justrelax.feature.timer.util.toFormattedTime

private data class TimerUiMetrics(
    val displaySize: TextUnit,
    val titleSize: TextUnit,
    val bodySize: TextUnit,
    val iconSize: Dp,
    val space1: Dp,
    val space2: Dp
)

@Composable
fun TimerCircularDisplay(
    totalTimeSeconds: Long,
    timeLeftSeconds: Long,
    modifier: Modifier = Modifier
) {
    val progressTarget =
        if (totalTimeSeconds > 0) timeLeftSeconds.toFloat() / totalTimeSeconds.toFloat() else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "ProgressAnim"
    )

    val targetColor = if (timeLeftSeconds in 1..5)
        MaterialTheme.colorScheme.error
    else
        MaterialTheme.colorScheme.primary

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(500),
        label = "ColorAnim"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth(0.9f)
            .aspectRatio(1f)
            .widthIn(max = 450.dp)
    ) {
        val windowSize = rememberWindowSizeClass()
        val metrics = when (windowSize) {
            WindowWidthSize.COMPACT -> TimerUiMetrics(
                displaySize = 48.sp,
                titleSize = 18.sp,
                bodySize = 16.sp,
                iconSize = 18.dp,
                space1 = 12.dp,
                space2 = 20.dp
            )
            WindowWidthSize.MEDIUM -> TimerUiMetrics(
                displaySize = 64.sp,
                titleSize = 24.sp,
                bodySize = 20.sp,
                iconSize = 22.dp,
                space1 = 16.dp,
                space2 = 28.dp
            )
            WindowWidthSize.EXPANDED -> TimerUiMetrics(
                displaySize = 80.sp,
                titleSize = 28.sp,
                bodySize = 22.sp,
                iconSize = 26.dp,
                space1 = 20.dp,
                space2 = 32.dp
            )
        }

        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = 12.dp,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            color = animatedColor,
            strokeWidth = 12.dp,
            trackColor = Color.Transparent,
            strokeCap = StrokeCap.Round
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatDurationVerbose(totalTimeSeconds),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = metrics.titleSize),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(metrics.space1))
            Text(
                text = timeLeftSeconds.toFormattedTime(),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = metrics.displaySize),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(metrics.space2))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(metrics.iconSize),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = calculateEndTime(timeLeftSeconds),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = metrics.bodySize),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}