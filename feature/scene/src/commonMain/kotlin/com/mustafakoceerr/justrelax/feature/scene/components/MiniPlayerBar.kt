package com.mustafakoceerr.justrelax.feature.scene.components

import com.mustafakoceerr.justrelax.core.ui.components.SoundIconBadge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.model.SoundIcon
import com.mustafakoceerr.justrelax.core.ui.extensions.rememberThrottledOnClick
import com.mustafakoceerr.justrelax.core.ui.generated.resources.action_pause
import com.mustafakoceerr.justrelax.core.ui.generated.resources.action_play
import com.mustafakoceerr.justrelax.core.ui.generated.resources.save_mix_dialog_title
import com.mustafakoceerr.justrelax.core.ui.icon.imageVector
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.scene.generated.resources.action_stop_all
import org.jetbrains.compose.resources.stringResource
import com.mustafakoceerr.justrelax.core.ui.generated.resources.Res as CoreRes

/** The scene that is playing, as a bar. The caller animates it in and out. */
@Composable
fun MiniPlayerBar(
    activeIcons: List<SoundIcon>,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onStopAllClick: () -> Unit,
    onSaveClick: () -> Unit,
    onOpenScene: () -> Unit,
    modifier: Modifier = Modifier
) {
    val debouncedOnPlayPauseClick = rememberThrottledOnClick(
        throttleMs = 500L,
        onClick = onPlayPauseClick
    )
    val debouncedOnSaveClick = rememberThrottledOnClick(
        throttleMs = 1000L,
        onClick = onSaveClick
    )
    val debouncedOnStopAllClick = rememberThrottledOnClick(
        throttleMs = 1000L,
        onClick = onStopAllClick
    )

    // Tapping the bar (outside its buttons) opens the scene panel.
    Surface(
        onClick = onOpenScene,
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shadowElevation = 8.dp,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LazyRow(
                modifier = Modifier.weight(0.8f),
                horizontalArrangement = Arrangement.spacedBy((-8).dp),
                contentPadding = PaddingValues(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(activeIcons) { icon ->
                    SoundIconBadge(
                        icon = icon,
                        borderColor = MaterialTheme.colorScheme.primaryContainer,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(2.dp),
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(0.6f, fill = false)
            ) {
                IconButton(
                    onClick = debouncedOnSaveClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FavoriteBorder,
                        contentDescription = stringResource(CoreRes.string.save_mix_dialog_title),
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = debouncedOnPlayPauseClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(if (isPlaying) CoreRes.string.action_pause else CoreRes.string.action_play),
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                IconButton(
                    onClick = debouncedOnStopAllClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(Res.string.action_stop_all),
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
