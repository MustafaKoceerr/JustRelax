package com.mustafakoceerr.justrelax.feature.settings.consent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.core.presentation.testing.TestTags
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.consent_accept
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.consent_decline
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.consent_later_hint
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.consent_point_never
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.consent_point_sent
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

/**
 * What would be sent and what never is, then two answers of equal size: declining is as easy as
 * agreeing, and either opens the app. The answers sit side by side where there is room.
 */
@Composable
fun ConsentScreen(
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
) {
    JustRelaxBackground {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val minHeight = maxHeight
            val sideBySide = maxWidth >= SIDE_BY_SIDE_MIN_WIDTH
            Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 520.dp)
                        .heightIn(min = minHeight)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                ) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(72.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Insights, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(36.dp))
                        }
                    }
                    Text(
                        stringResource(Res.string.consent_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Point(icon = Icons.Rounded.CheckCircle, text = stringResource(Res.string.consent_point_sent))
                            Point(icon = Icons.Rounded.Lock, text = stringResource(Res.string.consent_point_never))
                        }
                    }
                    TextButton(onClick = onOpenPrivacyPolicy) { Text(stringResource(Res.string.consent_privacy_policy)) }
                    Answers(sideBySide = sideBySide, onAccept = onAccept, onDecline = onDecline)
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
}

@Composable
private fun Point(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun Answers(sideBySide: Boolean, onAccept: () -> Unit, onDecline: () -> Unit) {
    val decline = @Composable { modifier: Modifier ->
        OutlinedButton(onClick = onDecline, modifier = modifier.testTag(TestTags.CONSENT_DECLINE)) {
            Text(stringResource(Res.string.consent_decline))
        }
    }
    val accept = @Composable { modifier: Modifier ->
        Button(onClick = onAccept, modifier = modifier.testTag(TestTags.CONSENT_ACCEPT)) {
            Text(stringResource(Res.string.consent_accept))
        }
    }
    if (sideBySide) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            decline(Modifier.weight(1f))
            accept(Modifier.weight(1f))
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            accept(Modifier.fillMaxWidth())
            decline(Modifier.fillMaxWidth())
        }
    }
}

private val SIDE_BY_SIDE_MIN_WIDTH = 440.dp
