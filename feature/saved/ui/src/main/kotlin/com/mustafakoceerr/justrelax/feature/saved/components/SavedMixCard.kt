package com.mustafakoceerr.justrelax.feature.saved.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.extensions.rememberThrottledOnClick
import com.mustafakoceerr.justrelax.feature.saved.SavedMixItem
import com.mustafakoceerr.justrelax.feature.saved.ui.R
import com.mustafakoceerr.justrelax.feature.saved.util.formatDate
import androidx.compose.ui.res.stringResource
import com.mustafakoceerr.justrelax.core.ui.R as CoreR

/**
 * A saved mix: cover, name, what's in it and a play/pause button. The one that plays is outlined
 * and says so. Delete is in the ⋮ menu and also a swipe towards the start.
 */
@Composable
fun SavedMixCard(
    mix: SavedMixItem,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    /** The card body; plays by default, selects the mix where a detail pane is shown. */
    onClick: () -> Unit = onTogglePlay,
    isSelected: Boolean = false,
) {
    val dismissState = rememberSwipeToDismissBoxState()
    LaunchedEffect(dismissState.targetValue) {
        if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) onDelete()
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = { DeleteBackground(isSwiping = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) },
        enableDismissFromStartToEnd = false,
        modifier = modifier,
    ) {
        CardContent(
            mix = mix,
            isPlaying = isPlaying,
            isSelected = isSelected,
            onClick = onClick,
            onTogglePlay = onTogglePlay,
            onDelete = onDelete,
        )
    }
}

@Composable
private fun CardContent(
    mix: SavedMixItem,
    isPlaying: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onTogglePlay: () -> Unit,
    onDelete: () -> Unit,
) {
    val throttledClick = rememberThrottledOnClick(throttleMs = 500L, onClick = onClick)
    val throttledToggle = rememberThrottledOnClick(throttleMs = 500L, onClick = onTogglePlay)
    val borderColor by animateColorAsState(
        targetValue = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        animationSpec = tween(250),
        label = "CardBorder",
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = tween(250),
        label = "CardContainer",
    )

    Surface(
        onClick = throttledClick,
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        border = BorderStroke(if (isPlaying) 1.5.dp else 1.dp, borderColor),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            SavedMixCover(icons = mix.icons, name = mix.title)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = mix.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                PlayingOrMetadata(mix = mix, isPlaying = isPlaying)
                Text(
                    text = mix.soundNames.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            PlayPauseButton(isPlaying = isPlaying, onClick = throttledToggle)
            MoreMenu(onDelete = onDelete)
        }
    }
}

@Composable
private fun PlayingOrMetadata(mix: SavedMixItem, isPlaying: Boolean) {
    val datePattern = stringResource(R.string.date_format_short)
    val date = remember(mix.date, datePattern) { formatDate(mix.date, datePattern) }

    AnimatedContent(targetState = isPlaying, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "PlayingState") { playing ->
        if (playing) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EqualizerBars(color = MaterialTheme.colorScheme.primary)
                Text(
                    text = stringResource(CoreR.string.sound_state_playing),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        } else {
            Text(
                text = stringResource(R.string.saved_mix_metadata, mix.icons.size, date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
internal fun PlayPauseButton(isPlaying: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledIconButton(onClick = onClick, modifier = modifier.size(48.dp)) {
        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.7f)) togetherWith fadeOut() },
            label = "PlayPauseIcon",
        ) { playing ->
            Icon(
                imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = stringResource(if (playing) CoreR.string.action_pause else CoreR.string.action_play),
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

@Composable
private fun MoreMenu(onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Rounded.MoreVert, contentDescription = stringResource(R.string.saved_more_options))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(CoreR.string.action_delete)) },
                leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
                onClick = {
                    expanded = false
                    onDelete()
                },
            )
        }
    }
}

@Composable
private fun DeleteBackground(isSwiping: Boolean) {
    val color by animateColorAsState(
        targetValue = if (isSwiping) MaterialTheme.colorScheme.errorContainer else Color.Transparent,
        label = "DeleteBackground",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color, MaterialTheme.shapes.large)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        if (isSwiping) {
            Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
        }
    }
}
