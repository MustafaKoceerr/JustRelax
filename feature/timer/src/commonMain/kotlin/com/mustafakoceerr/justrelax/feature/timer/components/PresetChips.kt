package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.feature.timer.util.formatDurationVerbose

/** Usual sleep lengths. A tap turns the wheels to it; the chip stays selected while they show it. */
@Composable
internal fun PresetChips(
    state: TimerPickerState,
    modifier: Modifier = Modifier,
) {
    val total = state.totalSeconds
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SLEEP_PRESETS_SECONDS.forEach { seconds ->
            FilterChip(
                selected = total == seconds,
                onClick = { state.set(seconds) },
                label = { Text(formatDurationVerbose(seconds)) },
            )
        }
    }
}
