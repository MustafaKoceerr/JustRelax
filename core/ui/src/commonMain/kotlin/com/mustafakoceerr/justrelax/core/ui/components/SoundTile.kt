package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.core.ui.extensions.rememberThrottledOnClick
import com.mustafakoceerr.justrelax.core.ui.generated.resources.Res
import com.mustafakoceerr.justrelax.core.ui.generated.resources.sound_action_download
import com.mustafakoceerr.justrelax.core.ui.generated.resources.sound_kind_event
import com.mustafakoceerr.justrelax.core.ui.generated.resources.sound_state_playing
import com.mustafakoceerr.justrelax.core.ui.generated.resources.sound_state_stopped
import com.mustafakoceerr.justrelax.core.ui.generated.resources.volume_level
import com.mustafakoceerr.justrelax.core.ui.icon.imageVector
import com.mustafakoceerr.justrelax.core.ui.util.volumeAfterDrag
import org.jetbrains.compose.resources.stringResource

private const val ANIM_DURATION = 300

/**
 * A sound in the library. A tap plays or stops it; while it plays, sliding across the tile sets
 * its volume, and the tile fills from the left as far as the volume goes. The fill is drawn from
 * [volume] in the draw phase, so a drag redraws the tile without recomposing it.
 */
@Composable
fun SoundTile(
    sound: LocalizedSound,
    isPlaying: Boolean,
    isDownloading: Boolean,
    volume: () -> Float,
    onClick: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    /** Set for a playing event sound, which then offers its frequency in the corner. */
    frequency: EventFrequency? = null,
    onFrequencyChange: (EventFrequency) -> Unit = {},
) {
    val throttledClick = rememberThrottledOnClick(throttleMs = 500, onClick = onClick)
    val latestVolume by rememberUpdatedState(volume)
    val latestOnVolumeChange by rememberUpdatedState(onVolumeChange)

    // Grows the fill in when the sound starts instead of showing it at once.
    val fillShown = animateFloatAsState(if (isPlaying) 1f else 0f, tween(ANIM_DURATION), label = "TileFill")
    val fillColor = MaterialTheme.colorScheme.primaryContainer
    val iconCircle by animateColorAsState(
        if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
        tween(ANIM_DURATION),
        label = "TileIconCircle",
    )
    val iconTint by animateColorAsState(
        if (isPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        tween(ANIM_DURATION),
        label = "TileIconTint",
    )
    val playingState = stringResource(if (isPlaying) Res.string.sound_state_playing else Res.string.sound_state_stopped)
    val volumeLabel = stringResource(Res.string.volume_level)

    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = modifier.aspectRatio(1f)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val shown = fillShown.value
                    if (shown > 0f) drawRect(fillColor, size = Size(size.width * latestVolume() * shown, size.height))
                }
                .then(
                    if (isPlaying) {
                        Modifier.pointerInput(Unit) {
                            detectHorizontalDragGestures { change, dragAmount ->
                                change.consume()
                                latestOnVolumeChange(volumeAfterDrag(latestVolume(), dragAmount, size.width.toFloat()))
                            }
                        }
                    } else {
                        Modifier
                    }
                )
                .clickable(role = Role.Button, onClick = throttledClick)
                .semantics {
                    contentDescription = sound.name
                    stateDescription = playingState
                    if (isPlaying) {
                        progressBarRangeInfo = ProgressBarRangeInfo(latestVolume(), 0f..1f)
                        setProgress(volumeLabel) { value ->
                            latestOnVolumeChange(value.coerceIn(0f, 1f))
                            true
                        }
                    }
                }
                .padding(12.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = iconCircle,
                modifier = Modifier
                    .size(40.dp)
                    .alpha(if (sound.isDownloaded) 1f else 0.4f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(sound.icon.imageVector, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                }
            }

            Corner(
                sound = sound,
                isPlaying = isPlaying,
                isDownloading = isDownloading,
                frequency = frequency,
                onFrequencyChange = onFrequencyChange,
                modifier = Modifier.align(Alignment.TopEnd),
            )

            Text(
                text = sound.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (sound.isDownloaded) 1f else 0.6f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.BottomStart),
            )
        }
    }
}

/** What the top-right corner says: download state, or an event's frequency (a picker while it plays). */
@Composable
private fun Corner(
    sound: LocalizedSound,
    isPlaying: Boolean,
    isDownloading: Boolean,
    frequency: EventFrequency?,
    onFrequencyChange: (EventFrequency) -> Unit,
    modifier: Modifier,
) {
    when {
        isDownloading -> CircularProgressIndicator(modifier = modifier.size(20.dp), strokeWidth = 2.dp)
        !sound.isDownloaded -> Icon(
            imageVector = Icons.Rounded.CloudDownload,
            contentDescription = stringResource(Res.string.sound_action_download),
            tint = MaterialTheme.colorScheme.primary,
            modifier = modifier.size(20.dp),
        )
        sound.kind != SoundKind.EVENT -> Unit
        isPlaying && frequency != null -> FrequencyChip(selected = frequency, onSelect = onFrequencyChange, compact = true, modifier = modifier)
        else -> Icon(
            imageVector = Icons.Rounded.Shuffle,
            contentDescription = stringResource(Res.string.sound_kind_event),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.size(16.dp),
        )
    }
}
