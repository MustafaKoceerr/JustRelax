package com.mustafakoceerr.justrelax.feature.scene.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.model.SoundIcon
import com.mustafakoceerr.justrelax.core.ui.components.SoundIconBadge
import com.mustafakoceerr.justrelax.core.ui.extensions.rememberThrottledOnClick
import com.mustafakoceerr.justrelax.core.ui.generated.resources.action_pause
import com.mustafakoceerr.justrelax.core.ui.generated.resources.action_play
import com.mustafakoceerr.justrelax.core.ui.generated.resources.sleep_timer
import com.mustafakoceerr.justrelax.core.ui.testing.TestTags
import com.mustafakoceerr.justrelax.core.ui.util.formatCountdown
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.action_stop_all
import org.jetbrains.compose.resources.stringResource
import com.mustafakoceerr.justrelax.core.ui.generated.resources.Res as CoreRes

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
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = names.joinToString(", "),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                TimerLeft(timerRemaining)
            }
            IconButton(onClick = onOpenTimer, modifier = Modifier.testTag(TestTags.MINI_PLAYER_TIMER)) {
                Icon(Icons.Rounded.Bedtime, contentDescription = stringResource(CoreRes.string.sleep_timer))
            }
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
                        contentDescription = stringResource(if (playing) CoreRes.string.action_pause else CoreRes.string.action_play),
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
            IconButton(onClick = throttledStopAll) {
                Icon(Icons.Rounded.Close, contentDescription = stringResource(Res.string.action_stop_all))
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
@Composable
private fun TimerLeft(timerRemaining: () -> Long?) {
    val remaining = timerRemaining()
    // The last time shown stays while the line fades out, instead of jumping to 0:00.
    val lastShown = remember { longArrayOf(0L) }
    if (remaining != null) lastShown[0] = remaining
    AnimatedVisibility(visible = remaining != null, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Bedtime, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = formatCountdown(lastShown[0]),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

private const val MAX_ICONS = 3
