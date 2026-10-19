package com.mustafakoceerr.justrelax.feature.ai.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.feature.ai.ui.R
import androidx.compose.ui.res.stringResource
import com.mustafakoceerr.justrelax.core.ui.R as CoreR

/** The mix the AI made: its name, why, what was asked, and what to do next. */
@Composable
fun AiMixHeader(
    name: String,
    description: String,
    prompt: String,
    isGenerating: Boolean,
    onRegenerate: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = AiSparkleIcon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
            Text(
                text = stringResource(R.string.ai_result_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
        Text(text = name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        if (description.isNotBlank()) {
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (prompt.isNotBlank()) {
            Text(
                text = stringResource(R.string.ai_prompt_quote, prompt),
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 4.dp),
        ) {
            FilledTonalButton(onClick = onRegenerate, enabled = !isGenerating) {
                AnimatedContent(targetState = isGenerating, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "RegenerateIcon") { loading ->
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
                Text(text = stringResource(R.string.ai_action_try_another_variation), modifier = Modifier.padding(start = 8.dp))
            }
            OutlinedButton(onClick = onSave, enabled = !isGenerating) {
                Icon(Icons.Outlined.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(text = stringResource(CoreR.string.action_save), modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
