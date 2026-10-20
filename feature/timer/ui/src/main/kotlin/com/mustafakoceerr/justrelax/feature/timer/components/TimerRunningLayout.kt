package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.feature.timer.TimerUiState

/** The countdown with its buttons: stacked, or side by side in a short window. */
@Composable
internal fun TimerRunningLayout(
    uiState: TimerUiState,
    dimensions: TimerDimensions,
    isCompactHeight: Boolean,
    onToggleClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    if (isCompactHeight) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TimerCountdown(uiState, dimensions, Modifier.fillMaxHeight(0.9f))
            TimerControlButtons(isPaused = uiState.isPaused, onToggleClick = onToggleClick, onCancelClick = onCancelClick)
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                // wrapContentWidth frees the fixed width again, so the cap and the square can shrink the
                // ring to fit a wide, short window (a tablet held sideways).
                TimerCountdown(uiState, dimensions, Modifier.fillMaxWidth(0.85f).wrapContentWidth().widthIn(max = 450.dp))
            }
            TimerControlButtons(isPaused = uiState.isPaused, onToggleClick = onToggleClick, onCancelClick = onCancelClick)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TimerCountdown(uiState: TimerUiState, dimensions: TimerDimensions, modifier: Modifier) {
    TimerCountdown(
        totalSeconds = uiState.totalSeconds,
        remainingSeconds = uiState.remainingSeconds,
        isPaused = uiState.isPaused,
        endsAt = uiState.endsAt,
        dimensions = dimensions,
        modifier = modifier,
    )
}
