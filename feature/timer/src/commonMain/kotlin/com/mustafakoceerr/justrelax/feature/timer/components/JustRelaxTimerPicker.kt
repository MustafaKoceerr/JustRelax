package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mustafakoceerr.justrelax.core.ui.util.WindowWidthSize
import justrelax.feature.timer.generated.resources.Res
import justrelax.feature.timer.generated.resources.timer_unit_hour
import justrelax.feature.timer.generated.resources.timer_unit_minute
import justrelax.feature.timer.generated.resources.timer_unit_second
import org.jetbrains.compose.resources.stringResource

private data class PickerUiMetrics(
    val itemHeight: Dp,
    val numberFontSize: TextUnit,
    val labelFontSize: TextUnit,
    val labelBottomPadding: Dp
)

@Composable
fun JustRelaxTimerPicker(
    windowSize: WindowWidthSize,
    modifier: Modifier = Modifier,
    state: JustRelaxTimerState = rememberJustRelaxTimerState()
) {
    val hours = remember { (0..23).map { it.toString().padStart(2, '0') } }
    val minutes = remember { (0..59).map { it.toString().padStart(2, '0') } }
    val seconds = remember { (0..59).map { it.toString().padStart(2, '0') } }

    val metrics = when (windowSize) {
        WindowWidthSize.COMPACT -> PickerUiMetrics(
            itemHeight = 80.dp,
            numberFontSize = 40.sp,
            labelFontSize = 15.sp,
            labelBottomPadding = 10.dp
        )

        WindowWidthSize.MEDIUM -> PickerUiMetrics(
            itemHeight = 96.dp,
            numberFontSize = 48.sp,
            labelFontSize = 17.sp,
            labelBottomPadding = 12.dp
        )

        WindowWidthSize.EXPANDED -> PickerUiMetrics(
            itemHeight = 110.dp,
            numberFontSize = 55.sp,
            labelFontSize = 18.sp,
            labelBottomPadding = 14.dp
        )
    }

    val textStyle = MaterialTheme.typography.displayLarge.copy(
        fontSize = metrics.numberFontSize,
        fontWeight = FontWeight.Bold
    )

    val labelStyle = MaterialTheme.typography.titleMedium.copy(
        fontSize = metrics.labelFontSize,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium
    )

    val separatorStyle = textStyle.copy(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    )

    val spacerHeight = metrics.labelFontSize.value.dp + metrics.labelBottomPadding
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimerUnitColumn(
            label = stringResource(Res.string.timer_unit_hour),
            labelStyle = labelStyle,
            paddingBottom = metrics.labelBottomPadding
        ) {
            InfiniteWheelPicker(
                itemHeight = metrics.itemHeight,
                items = hours,
                initialItem = state.hour.toString().padStart(2, '0'),
                textStyle = textStyle,
                onItemSelected = { _, index -> state.hour = index }
            )
        }

        TimerSeparator(
            separatorStyle = separatorStyle,
            spacerHeight = spacerHeight
        )

        TimerUnitColumn(
            label = stringResource(Res.string.timer_unit_minute),
            labelStyle = labelStyle,
            paddingBottom = metrics.labelBottomPadding
        ) {
            InfiniteWheelPicker(
                itemHeight = metrics.itemHeight,
                items = minutes,
                initialItem = state.minute.toString().padStart(2, '0'),
                textStyle = textStyle,
                onItemSelected = { _, index -> state.minute = index }
            )
        }

        TimerSeparator(
            separatorStyle = separatorStyle,
            spacerHeight = spacerHeight
        )

        TimerUnitColumn(
            label = stringResource(Res.string.timer_unit_second),
            labelStyle = labelStyle,
            paddingBottom = metrics.labelBottomPadding
        ) {
            InfiniteWheelPicker(
                itemHeight = metrics.itemHeight,
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
    paddingBottom: Dp,
    content: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            style = labelStyle,
            modifier = Modifier.padding(bottom = paddingBottom)
        )
        content()
    }
}

@Composable
private fun TimerSeparator(
    separatorStyle: TextStyle,
    spacerHeight: Dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(spacerHeight))
        Text(
            text = ":",
            style = separatorStyle,
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}