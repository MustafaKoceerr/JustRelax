package com.mustafakoceerr.justrelax.feature.saved.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.model.SoundIcon
import com.mustafakoceerr.justrelax.core.ui.generated.resources.action_play
import com.mustafakoceerr.justrelax.core.ui.icon.imageVector
import com.mustafakoceerr.justrelax.feature.saved.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.saved.generated.resources.date_format_short
import com.mustafakoceerr.justrelax.feature.saved.generated.resources.saved_mix_metadata
import com.mustafakoceerr.justrelax.feature.saved.util.formatDate
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant
import com.mustafakoceerr.justrelax.core.ui.generated.resources.Res as CoreRes

@Composable
fun SavedMixCard(
    title: String,
    date: Instant,
    soundCount: Int,
    icons: List<SoundIcon>,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** The card body; plays by default, selects the mix where a detail pane is shown. */
    onClick: () -> Unit = onPlayClick,
    isSelected: Boolean = false,
    shape: Shape = RoundedCornerShape(16.dp)
) {
    var clickTrigger by remember { mutableIntStateOf(0) }

    val baseColor = MaterialTheme.colorScheme.surfaceContainerLow
    val shimmerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    val dateFormatPattern = stringResource(Res.string.date_format_short)
    val formattedDate = remember(date, dateFormatPattern) {
        formatDate(date, dateFormatPattern)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(shape)
            .clickable {
                onClick()
                clickTrigger++
            }
            .clickShimmerEffect(
                trigger = clickTrigger,
                baseColor = baseColor,
                shimmerColor = shimmerColor
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
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
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = stringResource(Res.string.saved_mix_metadata, soundCount, formattedDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                MixIconsRow(icons, backgroundColor = baseColor)
            }

            Spacer(modifier = Modifier.width(16.dp))

            PlayButton(
                onClick = {
                    onPlayClick()
                    clickTrigger++
                }
            )
        }
    }
}

@Composable
private fun MixIconsRow(
    icons: List<SoundIcon>,
    backgroundColor: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy((-12).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icons.forEachIndexed { index, icon ->
            var isVisible by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                delay(index * 100L)
                isVisible = true
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(300)) + scaleIn(
                    animationSpec = tween(300),
                    initialScale = 0.5f
                )
            ) {
                Surface(
                    shape = CircleShape,
                    border = BorderStroke(
                        width = 2.dp,
                        color = backgroundColor
                    ),
                    modifier = Modifier.size(36.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon.imageVector,
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(1.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = stringResource(CoreRes.string.action_play),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun Modifier.clickShimmerEffect(
    trigger: Int,
    baseColor: Color,
    shimmerColor: Color
): Modifier {
    val shimmerProgress = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger > 0) {
            shimmerProgress.snapTo(0f)
            shimmerProgress.animateTo(
                targetValue = 2f,
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
            )
            shimmerProgress.snapTo(0f)
        }
    }

    return this.drawBehind {
        drawRect(baseColor)
        if (shimmerProgress.value > 0f) {
            val distance = size.width + size.height
            val currentOffset = distance * shimmerProgress.value
            val brush = Brush.linearGradient(
                colors = listOf(baseColor, shimmerColor, baseColor),
                start = Offset(currentOffset - size.width, currentOffset - size.height),
                end = Offset(currentOffset, currentOffset),
                tileMode = TileMode.Clamp
            )
            drawRect(brush = brush)
        }
    }
}