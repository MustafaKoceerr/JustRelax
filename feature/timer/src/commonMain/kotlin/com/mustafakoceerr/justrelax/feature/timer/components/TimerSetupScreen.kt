package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import justrelax.feature.timer.generated.resources.Res
import justrelax.feature.timer.generated.resources.timer_action_start
import org.jetbrains.compose.resources.stringResource

@Composable
fun TimerSetupScreen(
    onStartClick: (Long) -> Unit
) {
    val pickerState = rememberJustRelaxTimerState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            JustRelaxTimerPicker(state = pickerState)
        }

        Button(
            onClick = {
                val total = pickerState.totalSeconds
                if (total > 0) {
                    onStartClick(total)
                }
            },
            enabled = pickerState.totalSeconds > 0,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .widthIn(min = 120.dp)
                .height(56.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text(
                text = stringResource(Res.string.timer_action_start),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}