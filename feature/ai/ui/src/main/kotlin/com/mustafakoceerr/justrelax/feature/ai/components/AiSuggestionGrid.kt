package com.mustafakoceerr.justrelax.feature.ai.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** A ready-made prompt with an icon that hints at its mood. */
data class AiSuggestion(val text: String, val icon: ImageVector)

/** Ready-made prompts as tiles, two per row on phones and all in one row when there is room. */
@Composable
fun AiSuggestionGrid(
    suggestions: List<AiSuggestion>,
    enabled: Boolean,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columns = if (maxWidth >= ONE_ROW_MIN_WIDTH) suggestions.size.coerceAtLeast(1) else 2
        Column(verticalArrangement = Arrangement.spacedBy(GAP)) {
            suggestions.chunked(columns).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(GAP)) {
                    row.forEach { suggestion ->
                        SuggestionTile(
                            suggestion = suggestion,
                            enabled = enabled,
                            onClick = { onClick(suggestion.text) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // Keeps a short last row's tiles as wide as the others.
                    repeat(columns - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun SuggestionTile(suggestion: AiSuggestion, enabled: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.heightIn(min = 88.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = suggestion.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = suggestion.text,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private val GAP = 12.dp
private val ONE_ROW_MIN_WIDTH = 560.dp
