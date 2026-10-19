package com.mustafakoceerr.justrelax.feature.saved.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.core.ui.components.LayerKindLabel
import com.mustafakoceerr.justrelax.core.ui.components.LayerVolumeLevel
import com.mustafakoceerr.justrelax.core.ui.components.SoundLayerRow
import com.mustafakoceerr.justrelax.feature.saved.SavedMixDetail
import com.mustafakoceerr.justrelax.feature.saved.ui.R
import com.mustafakoceerr.justrelax.feature.saved.util.formatDate
import androidx.compose.ui.res.stringResource
import com.mustafakoceerr.justrelax.core.ui.R as CoreR

/** The selected saved mix beside the list on wide windows: its cover, its sounds, play and delete. */
@Composable
fun SavedMixDetailPane(
    detail: SavedMixDetail?,
    isPlaying: Boolean,
    onTogglePlay: (mixId: Long) -> Unit,
    onDelete: (mixId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = detail,
        contentKey = { it?.id },
        transitionSpec = { fadeIn(tween(220, delayMillis = 60)) togetherWith fadeOut(tween(90)) },
        label = "SavedDetail",
        modifier = modifier.fillMaxSize(),
    ) { shown ->
        if (shown == null) {
            DetailPlaceholder()
        } else {
            DetailContent(detail = shown, isPlaying = isPlaying, onTogglePlay = onTogglePlay, onDelete = onDelete)
        }
    }
}

@Composable
private fun DetailContent(
    detail: SavedMixDetail,
    isPlaying: Boolean,
    onTogglePlay: (mixId: Long) -> Unit,
    onDelete: (mixId: Long) -> Unit,
) {
    val datePattern = stringResource(R.string.date_format_short)
    val date = remember(detail.date, datePattern) { formatDate(detail.date, datePattern) }

    LazyColumn(
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "header") {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SavedMixCover(icons = detail.sounds.map { it.icon }, name = detail.title, size = 96.dp)
                    Spacer(modifier = Modifier.width(20.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = detail.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = stringResource(R.string.saved_mix_metadata, detail.sounds.size, date),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { onTogglePlay(detail.id) }) {
                        Icon(if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = null)
                        Text(
                            text = stringResource(if (isPlaying) CoreR.string.action_pause else CoreR.string.action_play),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    OutlinedButton(onClick = { onDelete(detail.id) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = null)
                        Text(text = stringResource(CoreR.string.action_delete), modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
        items(detail.sounds) { sound ->
            SoundLayerRow(
                icon = sound.icon,
                name = sound.name,
                isPlaying = isPlaying,
                supporting = {
                    LayerKindLabel(kind = if (sound.frequency == null) SoundKind.BED else SoundKind.EVENT, frequency = sound.frequency)
                },
                bottom = { LayerVolumeLevel(volume = sound.volume) },
            )
        }
    }
}

@Composable
private fun DetailPlaceholder() {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(max = 320.dp)) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(64.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = stringResource(R.string.saved_detail_placeholder),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
