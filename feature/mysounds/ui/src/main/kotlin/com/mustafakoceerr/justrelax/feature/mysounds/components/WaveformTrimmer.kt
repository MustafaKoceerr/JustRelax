package com.mustafakoceerr.justrelax.feature.mysounds.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.media.TrimRange
import kotlin.math.abs

/**
 * The file's waveform with the kept range highlighted between two handles. A drag moves whichever
 * handle is nearer to where it starts; the handles stop where the trim rules say.
 */
@Composable
fun WaveformTrimmer(
    peaks: List<Float>,
    sourceMs: Long,
    range: TrimRange,
    onMoveStart: (Long) -> Unit,
    onMoveEnd: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentRange by rememberUpdatedState(range)
    val kept = MaterialTheme.colorScheme.primary
    val dropped = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
    val handle = MaterialTheme.colorScheme.tertiary

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .pointerInput(sourceMs) {
                if (sourceMs <= 0) return@pointerInput
                var movesStart = true
                fun msAt(x: Float) = (x / size.width * sourceMs).toLong().coerceIn(0, sourceMs)
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        val at = msAt(offset.x)
                        movesStart = abs(at - currentRange.startMs) <= abs(at - currentRange.endMs)
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        val at = msAt(change.position.x)
                        if (movesStart) onMoveStart(at) else onMoveEnd(at)
                    },
                )
            },
    ) {
        if (peaks.isEmpty() || sourceMs <= 0) return@Canvas
        val barWidth = size.width / peaks.size
        val middle = size.height / 2
        peaks.forEachIndexed { index, peak ->
            val barMs = (index + 0.5f) / peaks.size * sourceMs
            val height = maxOf(2f, peak * size.height * 0.9f)
            drawRoundRect(
                color = if (barMs >= range.startMs && barMs <= range.endMs) kept else dropped,
                topLeft = Offset(index * barWidth + barWidth * 0.15f, middle - height / 2),
                size = Size(barWidth * 0.7f, height),
                cornerRadius = CornerRadius(barWidth / 3),
            )
        }
        listOf(range.startMs, range.endMs).forEach { ms ->
            val x = ms.toFloat() / sourceMs * size.width
            drawLine(handle, Offset(x, 0f), Offset(x, size.height), strokeWidth = 3.dp.toPx())
            drawCircle(handle, radius = 7.dp.toPx(), center = Offset(x, size.height - 7.dp.toPx()))
            drawCircle(handle, radius = 7.dp.toPx(), center = Offset(x, 7.dp.toPx()))
        }
    }
}
