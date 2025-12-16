package com.mustafakoceerr.justrelax.feature.saved.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.animation.core.Animatable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import justrelax.feature.saved.generated.resources.Res
import justrelax.feature.saved.generated.resources.action_delete
import justrelax.feature.saved.generated.resources.action_more_options
import justrelax.feature.saved.generated.resources.action_pause
import justrelax.feature.saved.generated.resources.action_play
import justrelax.feature.saved.generated.resources.action_rename
import justrelax.feature.saved.generated.resources.action_share
import justrelax.feature.saved.generated.resources.saved_mix_metadata
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
fun SavedMixInfo(
    title: String,
    soundCount: Int,
    date: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(
                Res.string.saved_mix_metadata,
                soundCount,
                date
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
fun SavedMixIcons(
    icons: List<ImageVector>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(icons.size) { index ->
            Icon(
                imageVector = icons[index],
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun SavedMixPlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor =
                if (isPlaying)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.secondaryContainer,
            contentColor =
                if (isPlaying)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Icon(
            imageVector =
                if (isPlaying)
                    Icons.Rounded.Pause
                else
                    Icons.Rounded.PlayArrow,
            contentDescription =
                stringResource(
                    if (isPlaying)
                        Res.string.action_pause
                    else
                        Res.string.action_play
                ),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun SavedMixMenu(
    onRenameClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(
                    Res.string.action_more_options
                ),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(
                            Res.string.action_rename
                        )
                    )
                },
                onClick = {
                    expanded = false
                    onRenameClick()
                },
                leadingIcon = {
                    Icon(Icons.Rounded.Edit, null)
                }
            )

            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(
                            Res.string.action_share
                        )
                    )
                },
                onClick = {
                    expanded = false
                    onShareClick()
                },
                leadingIcon = {
                    Icon(Icons.Rounded.Share, null)
                }
            )

            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(
                            Res.string.action_delete
                        ),
                        color = MaterialTheme.colorScheme.error
                    )
                },
                onClick = {
                    expanded = false
                    onDeleteClick()
                },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Delete,
                        null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }
}

@Composable
fun SavedMixCard(
    title: String,
    date: String,
    soundCount: Int,
    icons: List<String>,
    onPlayClick: () -> Unit,
    onRenameClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val baseColor = MaterialTheme.colorScheme.surfaceContainer
    val shimmerColor =
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
    val borderColor =
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    val cardShape = RoundedCornerShape(16.dp)

    val shimmerProgress = remember { Animatable(0f) }
    var clickTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(clickTrigger) {
        if (clickTrigger > 0) {
            shimmerProgress.snapTo(0f)
            shimmerProgress.animateTo(
                targetValue = 2f,
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = LinearEasing
                )
            )
            shimmerProgress.snapTo(0f)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(cardShape)
            .clickable {
                onPlayClick()
                clickTrigger++
            }
            .drawBehind {
                drawRect(baseColor)

                if (shimmerProgress.value > 0f) {
                    val distance = size.width + size.height
                    val currentOffset =
                        distance * shimmerProgress.value

                    val brush = Brush.linearGradient(
                        colors = listOf(
                            baseColor,
                            shimmerColor,
                            baseColor
                        ),
                        start = Offset(
                            currentOffset - size.width,
                            currentOffset - size.height
                        ),
                        end = Offset(
                            currentOffset,
                            currentOffset
                        ),
                        tileMode = TileMode.Clamp
                    )
                    drawRect(brush = brush)
                }
            },
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = stringResource(
                            Res.string.saved_mix_metadata,
                            soundCount,
                            date
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy((-12).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(icons.size) { index ->
                            Surface(
                                shape = CircleShape,
                                border = BorderStroke(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.size(36.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    AsyncImage(
                                        model = icons[index],
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .padding(2.dp),
                                        contentScale = ContentScale.Fit,
                                        colorFilter = ColorFilter.tint(
                                            MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Surface(
                    onClick = {
                        onPlayClick()
                        clickTrigger++
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = stringResource(
                                Res.string.action_play
                            ),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}