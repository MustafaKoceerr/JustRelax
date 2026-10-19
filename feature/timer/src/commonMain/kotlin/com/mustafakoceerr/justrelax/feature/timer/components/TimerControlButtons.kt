package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.generated.resources.action_cancel
import com.mustafakoceerr.justrelax.core.presentation.testing.TestTags
import com.mustafakoceerr.justrelax.feature.timer.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.timer.generated.resources.timer_action_pause
import com.mustafakoceerr.justrelax.feature.timer.generated.resources.timer_action_resume
import org.jetbrains.compose.resources.stringResource
import com.mustafakoceerr.justrelax.core.ui.generated.resources.Res as CoreRes

@Composable
internal fun TimerControlButtons(
    isPaused: Boolean,
    onToggleClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TimerPillButton(
            text = stringResource(CoreRes.string.action_cancel),
            onClick = onCancelClick,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.testTag(TestTags.TIMER_CANCEL),
        )
        TimerPillButton(
            text = stringResource(if (isPaused) Res.string.timer_action_resume else Res.string.timer_action_pause),
            icon = if (isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
            onClick = onToggleClick,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
