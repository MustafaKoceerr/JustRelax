package com.mustafakoceerr.justrelax.feature.scene.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.model.SoundIcon
import com.mustafakoceerr.justrelax.core.ui.components.SoundIconBadge
import com.mustafakoceerr.justrelax.core.ui.extensions.rememberThrottledOnClick
import com.mustafakoceerr.justrelax.core.presentation.testing.TestTags
import com.mustafakoceerr.justrelax.core.presentation.util.formatCountdown
import com.mustafakoceerr.justrelax.feature.scene.ui.R
import androidx.compose.ui.res.stringResource
import com.mustafakoceerr.justrelax.core.ui.R as CoreR

/**
 * The scene that is playing, as a bar: its sounds, the sleep timer's time left, and the timer,
 * play/pause and stop buttons. Tapping the bar elsewhere opens the scene panel. The caller animates
 * it in and out.
 */
@Composable
fun MiniPlayerBar(
    activeIcons: List<SoundIcon>,
    names: List<String>,
    isPlaying: Boolean,
    /** Seconds left on the sleep timer, or null; read only by the countdown, which ticks each second. */
    timerRemaining: () -> Long?,
    onPlayPauseClick: () -> Unit,
    onStopAllClick: () -> Unit,
    onOpenTimer: () -> Unit,
    onOpenScene: () -> Unit,
    modifier: Modifier = Modifier
) {
    val throttledPlayPause = rememberThrottledOnClick(throttleMs = 500L, onClick = onPlayPauseClick)
    val throttledStopAll = rememberThrottledOnClick(throttleMs = 1000L, onClick = onStopAllClick)

    Surface(
        onClick = onOpenScene,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconStack(icons = activeIcons)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = names.joinToString(", "),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            TimerChip(timerRemaining = timerRemaining, onClick = onOpenTimer)
            Spacer(modifier = Modifier.width(4.dp))
            FilledIconButton(
                onClick = throttledPlayPause,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                AnimatedContent(
                    targetState = isPlaying,
                    transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.7f)) togetherWith fadeOut() },
                    label = "MiniPlayPause",
                ) { playing ->
                    Icon(
                        imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(if (playing) CoreR.string.action_pause else CoreR.string.action_play),
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
            IconButton(onClick = throttledStopAll) {
                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.action_stop_all))
            }
        }
    }
}

/** Up to three icons overlapping; any more show as one "+n" badge. */
@Composable
private fun IconStack(icons: List<SoundIcon>) {
    Row(horizontalArrangement = Arrangement.spacedBy((-10).dp), verticalAlignment = Alignment.CenterVertically) {
        icons.take(MAX_ICONS).forEach { icon ->
            SoundIconBadge(
                icon = icon,
                borderColor = MaterialTheme.colorScheme.primaryContainer,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (icons.size > MAX_ICONS) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "+${icons.size - MAX_ICONS}", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

/** The sleep timer's countdown; the only part of the bar redrawn each second. */
/** The sleep timer, labelled so it is seen: "Sleep" when off, the time left while it runs. */
@Composable
private fun TimerChip(timerRemaining: () -> Long?, onClick: () -> Unit) {
    val timerDescription = stringResource(CoreR.string.sleep_timer)
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
        modifier = Modifier
            .heightIn(min = 36.dp)
            .testTag(TestTags.MINI_PLAYER_TIMER)
            .semantics { contentDescription = timerDescription },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Bedtime, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            TimerChipText(timerRemaining)
        }
    }
}

/** Reads the time left on its own, so the tick each second redraws only this text. */
@Composable
private fun TimerChipText(timerRemaining: () -> Long?) {
    val remaining = timerRemaining()
    Text(
        text = if (remaining != null) formatCountdown(remaining) else stringResource(R.string.scene_timer_chip),
        style = MaterialTheme.typography.labelLarge,
        maxLines = 1,
    )
}

private const val MAX_ICONS = 3
