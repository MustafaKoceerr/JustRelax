package com.mustafakoceerr.justrelax.feature.scene.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.generated.resources.action_cancel
import com.mustafakoceerr.justrelax.core.ui.generated.resources.sleep_timer
import com.mustafakoceerr.justrelax.core.presentation.testing.TestTags
import com.mustafakoceerr.justrelax.core.presentation.util.formatCountdown
import com.mustafakoceerr.justrelax.core.ui.util.formatDurationVerbose
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.scene_timer_change
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.scene_timer_custom
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.scene_timer_ends_in
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.scene_timer_hint
import org.jetbrains.compose.resources.stringResource
import com.mustafakoceerr.justrelax.core.ui.generated.resources.Res as CoreRes

/**
 * The sleep timer in the scene panel, so it is found where the sound is: one tap on a usual length
 * starts it, Custom opens the timer screen. While it runs, it shows the time left and can be
 * cancelled or changed.
 */
@Composable
internal fun SleepTimerCard(
    /** Seconds left, or null when no timer is set; read only by the countdown line, which ticks each second. */
    timerRemaining: () -> Long?,
    onStart: (seconds: Long) -> Unit,
    onCancel: () -> Unit,
    onOpenTimer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Changes only when the timer starts or stops, not on each tick.
    val currentRemaining by rememberUpdatedState(timerRemaining)
    val isRunning by remember { derivedStateOf { currentRemaining() != null } }

    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.large,
        modifier = modifier.fillMaxWidth().testTag(TestTags.SCENE_SLEEP_TIMER),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Rounded.Bedtime, contentDescription = null)
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(CoreRes.string.sleep_timer), style = MaterialTheme.typography.titleMedium)
                    if (isRunning) {
                        TimeLeft(timerRemaining)
                    } else {
                        Text(stringResource(Res.string.scene_timer_hint), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            AnimatedContent(targetState = isRunning, label = "SleepTimerCard") { running ->
                if (running) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End), modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = onOpenTimer) { Text(stringResource(Res.string.scene_timer_change)) }
                        FilledTonalButton(onClick = onCancel) { Text(stringResource(CoreRes.string.action_cancel)) }
                    }
                } else {
                    Presets(onStart = onStart, onOpenTimer = onOpenTimer)
                }
            }
        }
    }
}

@Composable
private fun TimeLeft(timerRemaining: () -> Long?) {
    val remaining = timerRemaining() ?: return
    Text(
        stringResource(Res.string.scene_timer_ends_in, formatCountdown(remaining)),
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun Presets(onStart: (seconds: Long) -> Unit, onOpenTimer: () -> Unit) {
    val chipColors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        PANEL_PRESETS_SECONDS.forEach { seconds ->
            AssistChip(
                onClick = { onStart(seconds) },
                label = { Text(formatDurationVerbose(seconds)) },
                colors = chipColors,
                border = null,
            )
        }
        AssistChip(
            onClick = onOpenTimer,
            label = { Text(stringResource(Res.string.scene_timer_custom)) },
            leadingIcon = { Icon(Icons.Rounded.Tune, contentDescription = null) },
            colors = chipColors,
            border = null,
        )
    }
}

/** The usual lengths, short enough to fit one row on a phone. */
private val PANEL_PRESETS_SECONDS: List<Long> = listOf(15, 30, 60).map { it * 60L }
