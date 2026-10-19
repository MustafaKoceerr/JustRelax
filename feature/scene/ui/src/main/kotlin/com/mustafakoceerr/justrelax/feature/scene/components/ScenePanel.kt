package com.mustafakoceerr.justrelax.feature.scene.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.core.ui.components.EmptyHint
import com.mustafakoceerr.justrelax.core.ui.components.LayerKindLabel
import com.mustafakoceerr.justrelax.core.ui.components.LayerVolumeSlider
import com.mustafakoceerr.justrelax.core.ui.components.SoundLayerRow
import com.mustafakoceerr.justrelax.feature.scene.SceneLayerItem
import com.mustafakoceerr.justrelax.feature.scene.SceneUiState
import com.mustafakoceerr.justrelax.feature.scene.ui.R
import androidx.compose.ui.res.stringResource
import com.mustafakoceerr.justrelax.core.ui.R as CoreR

/**
 * Every sound of the scene with its own controls: volume for all, how often for events. Shown in
 * a bottom sheet on phones and beside the library on wide windows.
 */
@Composable
fun ScenePanel(
    state: SceneUiState,
    volumeOf: (soundId: String) -> Float,
    onPlayPause: () -> Unit,
    onStopAll: () -> Unit,
    onSave: () -> Unit,
    onOpenTimer: () -> Unit,
    /** Seconds left on the sleep timer, or null; read only by the timer card's countdown. */
    timerRemaining: () -> Long?,
    onStartTimer: (seconds: Long) -> Unit,
    onCancelTimer: () -> Unit,
    onVolumeChange: (soundId: String, volume: Float) -> Unit,
    onFrequencyChange: (soundId: String, frequency: EventFrequency) -> Unit,
    onRemove: (soundId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.scene_panel_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onSave, enabled = state.isVisible) {
                Icon(Icons.Outlined.BookmarkAdd, contentDescription = stringResource(R.string.action_save_scene))
            }
            FilledIconButton(onClick = onPlayPause, enabled = state.isVisible) {
                if (state.isPlaying) {
                    Icon(Icons.Rounded.Pause, contentDescription = stringResource(CoreR.string.action_pause))
                } else {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = stringResource(CoreR.string.action_play))
                }
            }
            IconButton(onClick = onStopAll, enabled = state.isVisible) {
                Icon(Icons.Rounded.Stop, contentDescription = stringResource(R.string.action_stop_all))
            }
        }

        if (state.isVisible) {
            SleepTimerCard(
                timerRemaining = timerRemaining,
                onStart = onStartTimer,
                onCancel = onCancelTimer,
                onOpenTimer = onOpenTimer,
            )
        }

        if (state.layers.isEmpty()) {
            EmptyHint(stringResource(R.string.scene_empty))
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(state.layers, key = { it.soundId }) { layer ->
                    LayerControls(
                        layer = layer,
                        isPlaying = state.isPlaying,
                        volume = { volumeOf(layer.soundId) },
                        onVolumeChange = { onVolumeChange(layer.soundId, it) },
                        onFrequencyChange = { onFrequencyChange(layer.soundId, it) },
                        onRemove = { onRemove(layer.soundId) },
                    )
                }
            }
        }
    }
}

/** A layer of the scene with its volume, frequency for events, and a remove button. */
@Composable
private fun LayerControls(
    layer: SceneLayerItem,
    isPlaying: Boolean,
    /** Read only by the slider, so dragging it does not recompose the row. */
    volume: () -> Float,
    onVolumeChange: (Float) -> Unit,
    onFrequencyChange: (EventFrequency) -> Unit,
    onRemove: () -> Unit,
) {
    SoundLayerRow(
        icon = layer.icon,
        name = layer.name,
        isPlaying = isPlaying,
        supporting = {
            LayerKindLabel(
                kind = if (layer.frequency == null) SoundKind.BED else SoundKind.EVENT,
                frequency = layer.frequency,
                onFrequencyChange = onFrequencyChange,
            )
        },
        trailing = {
            IconButton(onClick = onRemove) {
                Icon(Icons.Rounded.Close, contentDescription = stringResource(CoreR.string.action_remove))
            }
        },
        bottom = { LayerVolumeSlider(volume = volume, onVolumeChange = onVolumeChange, enabled = true) },
    )
}
