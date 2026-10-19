package com.mustafakoceerr.justrelax.feature.mixer.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.ui.components.LayerKindLabel
import com.mustafakoceerr.justrelax.core.ui.components.LayerVolumeSlider
import com.mustafakoceerr.justrelax.core.ui.components.SceneCards
import com.mustafakoceerr.justrelax.core.ui.components.SoundLayerRow
import com.mustafakoceerr.justrelax.core.ui.generated.resources.action_save
import com.mustafakoceerr.justrelax.feature.mixer.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.mixer.generated.resources.mixer_current_mix
import com.mustafakoceerr.justrelax.feature.mixer.generated.resources.mixer_empty_body
import com.mustafakoceerr.justrelax.feature.mixer.generated.resources.mixer_empty_title
import com.mustafakoceerr.justrelax.feature.mixer.generated.resources.mixer_lock
import com.mustafakoceerr.justrelax.feature.mixer.generated.resources.mixer_lock_hint
import com.mustafakoceerr.justrelax.feature.mixer.generated.resources.mixer_unlock
import org.jetbrains.compose.resources.stringResource
import com.mustafakoceerr.justrelax.core.ui.generated.resources.Res as CoreRes

/**
 * One row per sound of the mix. New rows fade in one after another and the rest slide to their
 * new places, so a shuffle reads as the mix changing rather than the screen being replaced.
 */
internal fun LazyGridScope.mixLayerItems(
    sounds: List<LocalizedSound>,
    cards: SceneCards,
    lockedIds: Set<String>,
    volumeOf: (soundId: String) -> Float,
    onSoundClick: (soundId: String) -> Unit,
    onToggleLock: (soundId: String) -> Unit,
    onVolumeChange: (soundId: String, volume: Float) -> Unit,
    onFrequencyChange: (soundId: String, frequency: EventFrequency) -> Unit,
) {
    itemsIndexed(items = sounds, key = { _, sound -> sound.id }, contentType = { _, _ -> "layer" }) { index, sound ->
        val isPlaying = cards.isPlaying(sound.id)
        SoundLayerRow(
            icon = sound.icon,
            name = sound.name,
            isPlaying = isPlaying,
            onToggle = { onSoundClick(sound.id) },
            supporting = {
                LayerKindLabel(
                    kind = sound.kind,
                    frequency = cards.frequency(sound.id),
                    onFrequencyChange = { onFrequencyChange(sound.id, it) },
                )
            },
            trailing = { LockButton(isLocked = sound.id in lockedIds, onToggle = { onToggleLock(sound.id) }) },
            bottom = {
                LayerVolumeSlider(
                    volume = { volumeOf(sound.id) },
                    onVolumeChange = { onVolumeChange(sound.id, it) },
                    enabled = isPlaying,
                )
            },
            modifier = Modifier.animateItem(
                fadeInSpec = tween(durationMillis = 250, delayMillis = index * STAGGER_MS),
                placementSpec = spring(stiffness = Spring.StiffnessMediumLow),
                fadeOutSpec = tween(durationMillis = 150),
            ),
        )
    }
}

@Composable
private fun LockButton(isLocked: Boolean, onToggle: () -> Unit) {
    IconToggleButton(
        checked = isLocked,
        onCheckedChange = { onToggle() },
        colors = IconButtonDefaults.iconToggleButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            checkedContentColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Crossfade(targetState = isLocked, animationSpec = tween(200), label = "LockIcon") { locked ->
            Icon(
                imageVector = if (locked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                contentDescription = stringResource(if (locked) Res.string.mixer_unlock else Res.string.mixer_lock),
            )
        }
    }
}

/** "This mix" with a save button, and a reminder of what locking does once something is locked. */
@Composable
internal fun MixResultHeader(hasLocks: Boolean, onSave: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.mixer_current_mix),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            FilledTonalButton(onClick = onSave) {
                Icon(Icons.Outlined.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(text = stringResource(CoreRes.string.action_save), modifier = Modifier.padding(start = 8.dp))
            }
        }
        AnimatedVisibility(visible = hasLocks, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Text(
                text = stringResource(Res.string.mixer_lock_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/** Before the first shuffle: what the mixer does, in two lines. */
@Composable
internal fun MixerEmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(72.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
            Text(
                text = stringResource(Res.string.mixer_empty_title),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(Res.string.mixer_empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private const val STAGGER_MS = 40
