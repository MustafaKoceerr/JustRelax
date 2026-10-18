package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.testing.TestTags
import com.mustafakoceerr.justrelax.feature.timer.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.timer.generated.resources.timer_action_start
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TimerSetupScreen(
    dimensions: TimerDimensions,
    isCompactHeight: Boolean,
    onStartClick: (Long) -> Unit,
) {
    val pickerState = rememberTimerPickerState()

    if (isCompactHeight) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TimerPicker(state = pickerState, dimensions = dimensions)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PresetChips(state = pickerState, modifier = Modifier.widthIn(max = 320.dp))
                Spacer(modifier = Modifier.height(16.dp))
                StartButton(state = pickerState, onStartClick = onStartClick)
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            TimerPicker(state = pickerState, dimensions = dimensions)
            Spacer(modifier = Modifier.height(32.dp))
            PresetChips(state = pickerState, modifier = Modifier.widthIn(max = 480.dp))
            Spacer(modifier = Modifier.weight(1f))
            StartButton(state = pickerState, onStartClick = onStartClick)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/** Reads the picker on its own, so only the button recomposes when the wheels settle. */
@Composable
private fun StartButton(state: TimerPickerState, onStartClick: (Long) -> Unit) {
    val total = state.totalSeconds
    TimerPillButton(
        text = stringResource(Res.string.timer_action_start),
        onClick = { if (total > 0) onStartClick(total) },
        enabled = total > 0,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.testTag(TestTags.TIMER_START),
    )
}
