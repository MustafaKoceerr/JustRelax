package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.model.SoundIcon
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.core.ui.generated.resources.Res
import com.mustafakoceerr.justrelax.core.ui.generated.resources.frequency_title
import com.mustafakoceerr.justrelax.core.ui.generated.resources.sound_kind_bed
import com.mustafakoceerr.justrelax.core.ui.generated.resources.sound_kind_event
import com.mustafakoceerr.justrelax.core.ui.generated.resources.sound_state_playing
import com.mustafakoceerr.justrelax.core.ui.generated.resources.sound_state_stopped
import com.mustafakoceerr.justrelax.core.ui.icon.imageVector
import org.jetbrains.compose.resources.stringResource

private const val ANIM_COLOR_DURATION = 250

/**
 * One sound of a mix as a row: its icon (a play/stop toggle when [onToggle] is set), name, a
 * [supporting] line such as [LayerKindLabel], [trailing] actions and a [bottom] control such as
 * [LayerVolumeSlider].
 */
@Composable
fun SoundLayerRow(
    icon: SoundIcon,
    name: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onToggle: (() -> Unit)? = null,
    supporting: @Composable () -> Unit = {},
    trailing: @Composable RowScope.() -> Unit = {},
    bottom: @Composable () -> Unit = {},
) {
    val containerColor by animateColorAsState(
        targetValue = if (isPlaying) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = tween(ANIM_COLOR_DURATION),
        label = "LayerContainerColor",
    )

    Surface(modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = containerColor) {
        Column(modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LayerIcon(icon = icon, name = name, isPlaying = isPlaying, onToggle = onToggle)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    supporting()
                }
                trailing()
            }
            Box(modifier = Modifier.padding(end = 8.dp)) { bottom() }
        }
    }
}

@Composable
private fun LayerIcon(icon: SoundIcon, name: String, isPlaying: Boolean, onToggle: (() -> Unit)?) {
    val circleColor by animateColorAsState(
        targetValue = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
        animationSpec = tween(ANIM_COLOR_DURATION),
        label = "LayerIconCircle",
    )
    val tint by animateColorAsState(
        targetValue = if (isPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(ANIM_COLOR_DURATION),
        label = "LayerIconTint",
    )
    val state = stringResource(if (isPlaying) Res.string.sound_state_playing else Res.string.sound_state_stopped)
    val content: @Composable () -> Unit = {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon.imageVector, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        }
    }
    val iconModifier = Modifier
        .size(44.dp)
        .semantics {
            contentDescription = name
            stateDescription = state
        }

    if (onToggle != null) {
        Surface(checked = isPlaying, onCheckedChange = { onToggle() }, shape = CircleShape, color = circleColor, modifier = iconModifier, content = content)
    } else {
        Surface(shape = CircleShape, color = circleColor, modifier = iconModifier, content = content)
    }
}

/**
 * What kind of sound a layer is: a bed plays all the time; an event plays now and then, at a
 * [frequency] that can be changed through a [FrequencyChip] while it plays.
 */
@Composable
fun LayerKindLabel(
    kind: SoundKind,
    frequency: EventFrequency?,
    modifier: Modifier = Modifier,
    onFrequencyChange: ((EventFrequency) -> Unit)? = null,
) {
    when {
        kind == SoundKind.EVENT && frequency != null && onFrequencyChange != null ->
            FrequencyChip(selected = frequency, onSelect = onFrequencyChange, modifier = modifier)

        else -> Text(
            text = when {
                kind == SoundKind.BED -> stringResource(Res.string.sound_kind_bed)
                frequency != null -> frequency.label()
                else -> stringResource(Res.string.sound_kind_event)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier,
        )
    }
}

/**
 * An event's frequency as a small chip; a tap lists Rare, Normal and Often. [compact] drops the
 * arrow and shrinks the text, for a sound tile's corner.
 */
@Composable
fun FrequencyChip(
    selected: EventFrequency,
    onSelect: (EventFrequency) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    val title = stringResource(Res.string.frequency_title)
    val label = selected.label()

    Box(modifier = modifier) {
        AssistChip(
            onClick = { expanded = true },
            label = {
                Text(
                    text = label,
                    style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                )
            },
            trailingIcon = if (compact) {
                null
            } else {
                { Icon(Icons.Rounded.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp)) }
            },
            shape = CircleShape,
            border = null,
            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier
                .height(if (compact) 24.dp else 28.dp)
                .semantics { contentDescription = "$title: $label" },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            EventFrequency.entries.forEach { frequency ->
                DropdownMenuItem(
                    text = { Text(frequency.label()) },
                    onClick = {
                        expanded = false
                        onSelect(frequency)
                    },
                    trailingIcon = if (frequency == selected) {
                        { Icon(Icons.Rounded.Check, contentDescription = null) }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

/** A layer's volume. Only this reads [volume], so a drag redraws the slider and not the row. */
@Composable
fun LayerVolumeSlider(
    volume: () -> Float,
    onVolumeChange: (Float) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    VolumeSlider(
        value = volume(),
        onValueChange = onVolumeChange,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 8.dp, bottom = 8.dp),
    )
}

/** A saved layer's volume, shown but not changed. */
@Composable
fun LayerVolumeLevel(volume: Float, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = { volume },
        strokeCap = StrokeCap.Round,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        gapSize = 0.dp,
        drawStopIndicator = {},
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 12.dp, bottom = 12.dp),
    )
}
