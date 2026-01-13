package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(modifier = Modifier.weight(0.2f))

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ){
            TimerCircularDisplay(
                totalTimeSeconds = totalTimeSeconds,
                timeLeftSeconds = timeLeftSeconds
            )
        }

        Box(
            modifier = Modifier.weight(0.3f),
            contentAlignment = Alignment.Center
        ){
            TimerControlButtons(
                isPaused = isPaused,
                onToggleClick = onToggleClick,
                onCancelClick = onCancelClick,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}