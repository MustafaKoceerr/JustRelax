package com.mustafakoceerr.justrelax.feature.saved.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.components.label
import com.mustafakoceerr.justrelax.core.ui.icon.imageVector
import com.mustafakoceerr.justrelax.feature.saved.SavedMixDetail
import com.mustafakoceerr.justrelax.feature.saved.SavedSoundItem
import com.mustafakoceerr.justrelax.feature.saved.util.formatDate
import justrelax.feature.saved.generated.resources.Res
import justrelax.feature.saved.generated.resources.action_delete
import justrelax.feature.saved.generated.resources.action_play
import justrelax.feature.saved.generated.resources.date_format_short
import justrelax.feature.saved.generated.resources.saved_detail_placeholder
import org.jetbrains.compose.resources.stringResource

/** The selected saved mix beside the list on wide windows: its sounds, play and delete. */
@Composable
fun SavedMixDetailPane(
    detail: SavedMixDetail?,
    onPlay: (mixId: Long) -> Unit,
    onDelete: (mixId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (detail == null) {
        Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(Res.string.saved_detail_placeholder),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    val datePattern = stringResource(Res.string.date_format_short)
    val date = remember(detail.date, datePattern) { formatDate(detail.date, datePattern) }

    Column(modifier = modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = detail.title, style = MaterialTheme.typography.headlineSmall)
        Text(text = date, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { onPlay(detail.id) }) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                Text(stringResource(Res.string.action_play), modifier = Modifier.padding(start = 8.dp))
            }
            OutlinedButton(onClick = { onDelete(detail.id) }) {
                Icon(Icons.Rounded.Delete, contentDescription = null)
                Text(stringResource(Res.string.action_delete), modifier = Modifier.padding(start = 8.dp))
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(detail.sounds) { sound -> SavedSoundRow(sound) }
        }
    }
}

@Composable
private fun SavedSoundRow(sound: SavedSoundItem) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(
            imageVector = sound.icon.imageVector,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = sound.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            LinearProgressIndicator(progress = { sound.volume }, modifier = Modifier.fillMaxWidth())
        }
        sound.frequency?.let { frequency ->
            Text(text = frequency.label(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
