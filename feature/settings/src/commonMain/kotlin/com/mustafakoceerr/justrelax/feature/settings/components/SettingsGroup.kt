package com.mustafakoceerr.justrelax.feature.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * A titled group of settings rows. The rows sit 2 dp apart on one tone, with large outer corners
 * and small inner ones, so the group reads as one block and each row as a part of it.
 */
@Composable
fun SettingsGroup(title: String, modifier: Modifier = Modifier, rows: SettingsGroupScope.() -> Unit) {
    val items = SettingsGroupScope().apply(rows).items
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            items.forEachIndexed { index, item ->
                Surface(
                    shape = rowShape(isFirst = index == 0, isLast = index == items.lastIndex),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column { item() }
                }
            }
        }
    }
}

/** Collects the rows of a [SettingsGroup]; leaving a row out (e.g. behind an if) keeps the corners right. */
class SettingsGroupScope internal constructor() {
    internal val items = mutableListOf<@Composable ColumnScope.() -> Unit>()

    fun row(content: @Composable ColumnScope.() -> Unit) {
        items += content
    }
}

private fun rowShape(isFirst: Boolean, isLast: Boolean) = RoundedCornerShape(
    topStart = if (isFirst) OUTER_CORNER else INNER_CORNER,
    topEnd = if (isFirst) OUTER_CORNER else INNER_CORNER,
    bottomStart = if (isLast) OUTER_CORNER else INNER_CORNER,
    bottomEnd = if (isLast) OUTER_CORNER else INNER_CORNER,
)

private val OUTER_CORNER = 24.dp
private val INNER_CORNER = 6.dp

/** One setting: an icon, a title with an optional subtitle, and a chevron or other trailing content. */
@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    onClick: (() -> Unit)?,
    subtitle: String? = null,
    trailing: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    },
    below: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SettingsIcon(icon)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                if (subtitle != null) {
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
        below()
    }
}

@Composable
internal fun SettingsIcon(icon: ImageVector) {
    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(40.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}
