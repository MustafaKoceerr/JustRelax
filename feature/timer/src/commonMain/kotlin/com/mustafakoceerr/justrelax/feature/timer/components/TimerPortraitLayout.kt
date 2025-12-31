package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.domain.timer.TimerStatus
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TimerPortraitLayout(
    totalTimeSeconds: Long,
    timeLeftSeconds: Long,
    isPaused: Boolean,
    onToggleClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        TimerCircularDisplay(
            totalTimeSeconds = totalTimeSeconds,
            timeLeftSeconds = timeLeftSeconds
        )

        Spacer(modifier = Modifier.weight(1f))

        TimerControlButtons(
            isPaused = isPaused,
            onToggleClick = onToggleClick,
            onCancelClick = onCancelClick,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}
