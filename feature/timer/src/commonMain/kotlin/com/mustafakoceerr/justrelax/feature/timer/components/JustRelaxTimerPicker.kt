package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import justrelax.feature.timer.generated.resources.Res
import justrelax.feature.timer.generated.resources.timer_unit_hour
import justrelax.feature.timer.generated.resources.timer_unit_minute
import justrelax.feature.timer.generated.resources.timer_unit_second
import org.jetbrains.compose.resources.stringResource

@Composable
fun JustRelaxTimerPicker(
    modifier: Modifier = Modifier,
    state: JustRelaxTimerState = rememberJustRelaxTimerState()
) {
    val hours = remember { (0..23).map { it.toString().padStart(2, '0') } }
    val minutes = remember { (0..59).map { it.toString().padStart(2, '0') } }
    val seconds = remember { (0..59).map { it.toString().padStart(2, '0') } }

    val textStyle = MaterialTheme.typography.displayLarge.copy(
        fontSize = 50.sp,
        fontWeight = FontWeight.Bold
    )

    val labelStyle = MaterialTheme.typography.titleMedium.copy(
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium
    )

    val separatorStyle = textStyle.copy(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    )

    val itemHeight = 100.dp

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimerUnitColumn(
            label = stringResource(Res.string.timer_unit_hour),
            labelStyle = labelStyle
        ) {
            InfiniteWheelPicker(
                itemHeight = itemHeight,
                items = hours,
                initialItem = state.hour.toString().padStart(2, '0'),
                textStyle = textStyle,
                onItemSelected = { _, index -> state.hour = index }
            )
        }

        TimerSeparator(separatorStyle)

        TimerUnitColumn(
            label = stringResource(Res.string.timer_unit_minute),
            labelStyle = labelStyle
        ) {
            InfiniteWheelPicker(
                itemHeight = itemHeight,
                items = minutes,
                initialItem = state.minute.toString().padStart(2, '0'),
                textStyle = textStyle,
                onItemSelected = { _, index -> state.minute = index }
            )
        }

        TimerSeparator(separatorStyle)

        TimerUnitColumn(
            label = stringResource(Res.string.timer_unit_second),
            labelStyle = labelStyle
        ) {
            InfiniteWheelPicker(
                itemHeight = itemHeight,
                items = seconds,
                initialItem = state.second.toString().padStart(2, '0'),
                textStyle = textStyle,
                onItemSelected = { _, index -> state.second = index }
            )
        }
    }
}

@Composable
private fun TimerUnitColumn(
    label: String,
    labelStyle: TextStyle,
    content: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            style = labelStyle,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        content()
    }
}

@Composable
private fun TimerSeparator(style: TextStyle) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(" ", style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp))
        Text(
            text = ":",
            style = style,
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}