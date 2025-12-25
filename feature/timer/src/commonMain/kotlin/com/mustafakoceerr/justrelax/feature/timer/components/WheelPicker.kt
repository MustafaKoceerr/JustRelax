package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import justrelax.feature.timer.generated.resources.Res
import justrelax.feature.timer.generated.resources.timer_action_start
import justrelax.feature.timer.generated.resources.timer_unit_hour
import justrelax.feature.timer.generated.resources.timer_unit_minute
import justrelax.feature.timer.generated.resources.timer_unit_second
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InfiniteWheelPicker(
    modifier: Modifier = Modifier,
    width: Dp = 90.dp,
    itemHeight: Dp = 100.dp,
    items: List<String>,
    initialItem: String,
    textStyle: TextStyle = MaterialTheme.typography.displayMedium,
    activeColor: Color = MaterialTheme.colorScheme.onSurface,
    inactiveColor: Color = activeColor.copy(alpha = 0.2f),
    onItemSelected: (String, Int) -> Unit
) {
    val largeCount = Int.MAX_VALUE
    val startIndex = largeCount / 2

    val initialIndex = remember(items, initialItem) {
        val index = items.indexOf(initialItem)
        if (index != -1) startIndex + index - (startIndex % items.size) else startIndex
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val centerIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val actualIndex = centerIndex % items.size
            onItemSelected(items[actualIndex], actualIndex)
        }
    }

    val brush = remember(activeColor, inactiveColor) {
        Brush.verticalGradient(
            0.0f to inactiveColor,
            0.35f to inactiveColor,
            0.35f to activeColor,
            0.65f to activeColor,
            0.65f to inactiveColor,
            1.0f to inactiveColor
        )
    }

    Box(
        modifier = modifier
            .width(width)
            .height(itemHeight * 3),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()
                        drawRect(brush = brush, blendMode = BlendMode.SrcIn)
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = itemHeight)
        ) {
            items(largeCount) { index ->
                val item = items[index % items.size]

                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        style = textStyle,
                        color = Color.Black,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

class JustRelaxTimerState(
    initialHour: Int,
    initialMinute: Int,
    initialSecond: Int
) {
    var hour by mutableIntStateOf(initialHour)
    var minute by mutableIntStateOf(initialMinute)
    var second by mutableIntStateOf(initialSecond)

    val totalSeconds: Long
        get() = (hour * 3600L) + (minute * 60L) + second

    companion object {
        val Saver: Saver<JustRelaxTimerState, *> = listSaver(
            save = { listOf(it.hour, it.minute, it.second) },
            restore = { JustRelaxTimerState(it[0], it[1], it[2]) }
        )
    }
}

@Composable
fun rememberJustRelaxTimerState(
    initialHour: Int = 0,
    initialMinute: Int = 0,
    initialSecond: Int = 0,
): JustRelaxTimerState {
    return rememberSaveable(saver = JustRelaxTimerState.Saver) {
        JustRelaxTimerState(initialHour, initialMinute, initialSecond)
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
fun JustRelaxTimerPicker(
    modifier: Modifier = Modifier,
    state: JustRelaxTimerState = rememberJustRelaxTimerState()
) {
    val hours = remember { (0..24).map { it.toString().padStart(2, '0') } }
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

        TimerUnitColumn(label = " ", labelStyle = labelStyle) {
            Text(
                text = ":",
                style = separatorStyle,
                modifier = Modifier.padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

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

        TimerUnitColumn(label = " ", labelStyle = labelStyle) {
            Text(
                text = ":",
                style = separatorStyle,
                modifier = Modifier.padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

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
            JustRelaxTimerPicker(
                state = pickerState
            )
        }

        Button(
            onClick = {
                val total = pickerState.totalSeconds
                if (total > 0) {
                    onStartClick(total)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .widthIn(min = 120.dp)
                .height(56.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text(
                text = stringResource(
                    Res.string.timer_action_start
                ),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}