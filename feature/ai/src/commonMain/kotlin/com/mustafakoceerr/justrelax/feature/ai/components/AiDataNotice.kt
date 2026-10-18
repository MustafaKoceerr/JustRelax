package com.mustafakoceerr.justrelax.feature.ai.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.ai_data_notice_accept
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.ai_data_notice_body
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.ai_data_notice_dismiss
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.ai_data_notice_privacy
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.ai_data_notice_short
import com.mustafakoceerr.justrelax.feature.ai.generated.resources.ai_data_notice_title
import org.jetbrains.compose.resources.stringResource

/** Asked once, before the first mix: the prompt goes to OpenAI, which may use it to improve its models. */
@Composable
fun AiDataNoticeDialog(
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
        title = { Text(stringResource(Res.string.ai_data_notice_title), textAlign = TextAlign.Center) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.ai_data_notice_body), style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onOpenPrivacyPolicy) { Text(stringResource(Res.string.ai_data_notice_privacy)) }
            }
        },
        confirmButton = { TextButton(onClick = onAccept) { Text(stringResource(Res.string.ai_data_notice_accept)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.ai_data_notice_dismiss)) } },
    )
}

/** The same notice in one line, under the prompt field. */
@Composable
fun AiDataNoticeLine(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.ai_data_notice_short),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}
