package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import justrelax.feature.timer.generated.resources.Res
import justrelax.feature.timer.generated.resources.timer_action_delete
import justrelax.feature.timer.generated.resources.timer_action_pause
import justrelax.feature.timer.generated.resources.timer_action_resume
import org.jetbrains.compose.resources.stringResource

@Composable
fun TimerControlButtons(
    isPaused: Boolean,
    onToggleClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalButton(
            onClick = onCancelClick,
            modifier = Modifier
                .height(50.dp)
                .widthIn(min = 100.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(Res.string.timer_action_delete),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(32.dp))

        val isRunning = !isPaused
        val toggleContainerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        val toggleIcon = if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow
        val toggleText = if (isRunning) Res.string.timer_action_pause else Res.string.timer_action_resume

        Button(
            onClick = onToggleClick,
            modifier = Modifier
                .height(50.dp)
                .widthIn(min = 130.dp),
            contentPadding = PaddingValues(horizontal = 24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = toggleContainerColor
            )
        ) {
            Icon(
                imageVector = toggleIcon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(toggleText),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}