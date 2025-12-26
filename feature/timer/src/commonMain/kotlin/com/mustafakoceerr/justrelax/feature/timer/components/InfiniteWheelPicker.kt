package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InfiniteWheelPicker(
    items: List<String>,
    initialItem: String,
    onItemSelected: (String, Int) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 90.dp,
    itemHeight: Dp = 100.dp,
    textStyle: TextStyle = MaterialTheme.typography.displayMedium,
    activeColor: Color = MaterialTheme.colorScheme.onSurface,
    inactiveColor: Color = activeColor.copy(alpha = 0.2f),
) {
    val largeCount = Int.MAX_VALUE
    val startIndex = largeCount / 2

    // Başlangıç indeksini hesapla (Listenin ortasına denk gelen doğru item)
    val initialIndex = remember(items, initialItem) {
        val index = items.indexOf(initialItem)
        if (index != -1) startIndex + index - (startIndex % items.size) else startIndex
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // State okumalarını optimize et
    val centerIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }

    // Scroll durduğunda seçili öğeyi bildir
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val actualIndex = centerIndex % items.size
            // IndexOutOfBounds koruması
            if (actualIndex in items.indices) {
                onItemSelected(items[actualIndex], actualIndex)
            }
        }
    }

    // Gradient Maskesi (Üst ve alt kısımları silikleştirmek için)
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
            .height(itemHeight * 3), // Görünür alan: 3 satır yüksekliği
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier
                .fillMaxSize()
                // Offscreen stratejisi BlendMode.SrcIn'in doğru çalışması için kritiktir.
                // Bu sayede gradient sadece text üzerinde maskeleme yapar.
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()
                        drawRect(brush = brush, blendMode = BlendMode.SrcIn)
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = itemHeight) // Ortalamak için padding
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
                        color = Color.Black, // Renk Brush ile yönetiliyor, burası dummy
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}