package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.mustafakoceerr.justrelax.feature.timer.util.twoDigits
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * A drum of the numbers 0 until [count] that turns without end, three rows high.
 *
 * Items shrink, fade and tilt in the graphics layer, which reads the scroll position in the draw
 * phase, so turning the wheel does not recompose it. It ticks under the finger, reports the value
 * where it comes to rest and turns by itself when [selected] changes from outside (a chip, typing).
 */
@Composable
internal fun WheelPicker(
    count: Int,
    selected: Int,
    onSelected: (Int) -> Unit,
    itemHeight: Dp,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    /** Leaves the center row empty while a text field covers it. */
    hideCenter: Boolean = false,
    onCenterClick: () -> Unit = {},
    centerClickLabel: String? = null,
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndexFor(selected, count))
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }
    val currentOnSelected by rememberUpdatedState(onSelected)
    val restReporter = remember { WheelRestReporter() }

    LaunchedEffect(listState) {
        listState.interactionSource.interactions
            .filterIsInstance<DragInteraction.Start>()
            .collect { restReporter.onUserDrag() }
    }

    LaunchedEffect(listState, count) {
        snapshotFlow { listState.isScrollInProgress }
            .filter { inProgress -> !inProgress }
            .collect {
                restReporter.onRest(valueAt(listState.firstVisibleItemIndex, count))?.let(currentOnSelected)
            }
    }

    LaunchedEffect(listState, itemHeightPx) {
        snapshotFlow { listState.centerIndex(itemHeightPx) }
            .drop(1)
            .collect { haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick) }
    }

    LaunchedEffect(selected, count) {
        val current = listState.firstVisibleItemIndex
        // No check for a scroll in progress: a cut-short turn to the previous value may still be running.
        if (valueAt(current, count) != selected) {
            restReporter.onTurnTo(selected)
            listState.animateScrollToItem(nearestIndexFor(selected, current, count))
        }
    }

    Box(
        modifier = modifier
            .height(itemHeight * 3)
            .semantics { stateDescription = selected.twoDigits() },
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(count = wheelItemCount(count)) { index ->
                Text(
                    text = valueAt(index, count).twoDigits(),
                    style = textStyle,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clickable(interactionSource = null, indication = null, onClickLabel = centerClickLabel) {
                            if (index == listState.firstVisibleItemIndex) onCenterClick()
                            else scope.launch { listState.animateScrollToItem(index) }
                        }
                        .wrapContentHeight(Alignment.CenterVertically)
                        .graphicsLayer {
                            val distance = listState.distanceFromCenter(index)
                            val transform = wheelItemTransform(distance)
                            scaleX = transform.scale
                            scaleY = transform.scale
                            rotationX = transform.rotationX
                            alpha = if (hideCenter && abs(distance) < 0.5f) 0f else transform.alpha
                            cameraDistance = 12 * density
                        },
                )
            }
        }
    }
}

/** The item over the center row while the wheel moves. At rest that is the first visible item. */
private fun LazyListState.centerIndex(itemHeightPx: Float): Int =
    firstVisibleItemIndex + if (firstVisibleItemScrollOffset > itemHeightPx / 2) 1 else 0

/** Rows between [index] and the center (negative is above). Item offsets and the viewport share one origin. */
private fun LazyListState.distanceFromCenter(index: Int): Float {
    val info = layoutInfo
    val item = info.visibleItemsInfo.firstOrNull { it.index == index } ?: return Float.MAX_VALUE
    val center = info.viewportStartOffset + info.viewportSize.height / 2f
    return (item.offset + item.size / 2f - center) / item.size
}
