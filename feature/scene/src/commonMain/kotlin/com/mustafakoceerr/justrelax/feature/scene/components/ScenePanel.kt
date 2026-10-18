package com.mustafakoceerr.justrelax.feature.scene.components

import com.mustafakoceerr.justrelax.core.ui.components.EmptyHint
import com.mustafakoceerr.justrelax.core.ui.generated.resources.action_play
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FavoriteBorder
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.ui.components.FrequencySelector
import com.mustafakoceerr.justrelax.core.ui.components.VolumeSlider
import com.mustafakoceerr.justrelax.core.ui.icon.imageVector
import com.mustafakoceerr.justrelax.feature.scene.SceneLayerItem
import com.mustafakoceerr.justrelax.feature.scene.SceneUiState
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.action_pause
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.action_save_scene
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.action_stop_all
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.scene_empty
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.scene_panel_title
import com.mustafakoceerr.justrelax.core.ui.generated.resources.Res as CoreRes
import com.mustafakoceerr.justrelax.core.ui.generated.resources.action_remove
import org.jetbrains.compose.resources.stringResource

/**
 * Every sound of the scene with its own controls: volume for all, how often for events. Shown in
 * a bottom sheet on phones and beside the library on wide windows.
 */
@Composable
fun ScenePanel(
    state: SceneUiState,
    onPlayPause: () -> Unit,
    onStopAll: () -> Unit,
    onSave: () -> Unit,
    onVolumeChange: (soundId: String, volume: Float) -> Unit,
    onFrequencyChange: (soundId: String, frequency: EventFrequency) -> Unit,
    onRemove: (soundId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.scene_panel_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onSave, enabled = state.isVisible) {
                Icon(Icons.Rounded.FavoriteBorder, contentDescription = stringResource(Res.string.action_save_scene))
            }
            FilledIconButton(onClick = onPlayPause, enabled = state.isVisible) {
                if (state.isPlaying) {
                    Icon(Icons.Rounded.Pause, contentDescription = stringResource(Res.string.action_pause))
                } else {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = stringResource(CoreRes.string.action_play))
                }
            }
            IconButton(onClick = onStopAll, enabled = state.isVisible) {
                Icon(Icons.Rounded.Stop, contentDescription = stringResource(Res.string.action_stop_all))
            }
        }

        if (state.layers.isEmpty()) {
            EmptyHint(stringResource(Res.string.scene_empty))
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(state.layers, key = { it.soundId }) { layer ->
                    LayerControls(
                        layer = layer,
                        onVolumeChange = { onVolumeChange(layer.soundId, it) },
                        onFrequencyChange = { onFrequencyChange(layer.soundId, it) },
                        onRemove = { onRemove(layer.soundId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LayerControls(
    layer: SceneLayerItem,
    onVolumeChange: (Float) -> Unit,
    onFrequencyChange: (EventFrequency) -> Unit,
    onRemove: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(
                imageVector = layer.icon.imageVector,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = layer.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Rounded.Close, contentDescription = stringResource(CoreRes.string.action_remove))
            }
        }
        VolumeSlider(value = layer.volume, onValueChange = onVolumeChange, modifier = Modifier.fillMaxWidth())
        layer.frequency?.let { frequency ->
            FrequencySelector(selected = frequency, onSelect = onFrequencyChange, modifier = Modifier.fillMaxWidth())
        }
    }
}
