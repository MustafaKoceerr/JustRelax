package com.mustafakoceerr.justrelax.feature.settings.consent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.core.ui.testing.TestTags
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.consent_accept
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.consent_body
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.consent_decline
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.consent_later_hint
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.consent_privacy_policy
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.consent_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ConsentRoute(
    onDone: () -> Unit,
    viewModel: ConsentViewModel = koinViewModel(),
) {
    val isDone by viewModel.isDone.collectAsStateWithLifecycle()
    LaunchedEffect(isDone) { if (isDone) onDone() }

    ConsentScreen(
        onAccept = { viewModel.answer(granted = true) },
        onDecline = { viewModel.answer(granted = false) },
        onOpenPrivacyPolicy = viewModel::openPrivacyPolicy,
    )
}

/** Both answers are equal buttons: declining is as easy as agreeing, and either opens the app. */
@Composable
fun ConsentScreen(
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
) {
    JustRelaxBackground {
        Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.widthIn(max = 480.dp).verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    Icons.Rounded.Insights,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp),
                )
                Text(
                    stringResource(Res.string.consent_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    stringResource(Res.string.consent_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                TextButton(onClick = onOpenPrivacyPolicy) { Text(stringResource(Res.string.consent_privacy_policy)) }
                Button(onClick = onAccept, modifier = Modifier.fillMaxWidth().testTag(TestTags.CONSENT_ACCEPT)) { Text(stringResource(Res.string.consent_accept)) }
                OutlinedButton(onClick = onDecline, modifier = Modifier.fillMaxWidth().testTag(TestTags.CONSENT_DECLINE)) { Text(stringResource(Res.string.consent_decline)) }
                Text(
                    stringResource(Res.string.consent_later_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
