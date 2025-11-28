package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * SENIOR NOTU: INFINITE WHEEL PICKER MANTIĞI
 *
 * Bu bileşen "Sanal Sonsuzluk" (Virtual Infinity) kullanır.
 * Gerçekte sonsuz bir liste yoktur. Int.MAX_VALUE kadar büyük bir liste oluştururuz.
 * Kullanıcıyı bu listenin tam ortasından başlatırız.
 * Kullanıcı yukarı veya aşağı kaydırdığında aslında bu devasa listenin içinde gezer.
 * Modulo (%) operatörü ile [index % items.size] diyerek gerçek veriyi buluruz.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InfiniteWheelPicker(
    modifier: Modifier = Modifier,
    width: Dp = 90.dp,
    itemHeight: Dp = 100.dp, // Her bir satırın yüksekliği (Sabit olmalı)
    items: List<String>, // Gösterilecek veriler (00, 01, 02...)
    initialItem: String, // Başlangıçta seçili olacak değer
    textStyle: TextStyle = MaterialTheme.typography.displayMedium,
    activeColor: Color = MaterialTheme.colorScheme.onSurface, // Seçili olanın rengi (Parlak)
    inactiveColor: Color = activeColor.copy(alpha = 0.2f), // Seçili olmayanın rengi (Silik)
    onItemSelected: (String, Int) -> Unit // Seçim değiştiğinde tetiklenen callback
) {
    // 1. SANAL SONSUZLUK AYARLARI
    val largeCount = Int.MAX_VALUE
    val startIndex = largeCount / 2 // Listenin ortası

    // Başlangıç indeksini hesapla:
    // Kullanıcının istediği 'initialItem'ın, listenin ortasındaki en yakın karşılığını buluyoruz.
    val initialIndex = remember(items, initialItem) {
        val index = items.indexOf(initialItem)
        if (index != -1) startIndex + index - (startIndex % items.size) else startIndex
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    // 2. NATIVE SNAPPING (YAPIŞMA)
    // Kaydırma bitince tekerleğin "tık" diye en yakın sayıya oturmasını sağlayan native API.
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // 3. PERFORMANS OPTİMİZASYONU (derivedStateOf)
    // listState.firstVisibleItemIndex her piksel kaymasında değişir.
    // derivedStateOf kullanmazsak, her pikselde tüm ekranı yeniden çizer (Recomposition).
    // Bunu kullanarak sadece index değiştiğinde (yeni sayıya geçince) tetiklenmesini sağlıyoruz.
    val centerIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }

    // Seçim değişikliğini dinle ve dışarıya haber ver
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            // Modulo (%) ile gerçek indeksi buluyoruz (Örn: 10005 % 60 = 5)
            val actualIndex = centerIndex % items.size
            onItemSelected(items[actualIndex], actualIndex)
        }
    }

    // 4. GÖRSEL MASKELEME (GRADIENT BRUSH)
    // Üstü ve altı silik, ortası parlak yapan fırça.
    val brush = remember(activeColor, inactiveColor) {
        Brush.verticalGradient(
            0.0f to inactiveColor,
            0.35f to inactiveColor, // Üst kısım silik
            0.35f to activeColor,   // KESKİN GEÇİŞ: Buradan itibaren parlak
            0.65f to activeColor,   // Orta kısım parlak
            0.65f to inactiveColor, // KESKİN GEÇİŞ: Buradan itibaren silik
            1.0f to inactiveColor
        )
    }

    Box(
        modifier = modifier
            .width(width)
            .height(itemHeight * 3), // Ekranda her zaman 3 satır görünür
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier
                .fillMaxSize()
                // --- SENIOR GRAFİK HİLESİ ---
                // CompositingStrategy.Offscreen: İçeriği önce sanal bir katmana çizer.
                // BlendMode.SrcIn: Fırçayı (Gradient) sadece metinlerin üzerine uygular.
                // Sonuç: Metinler yukarı çıktıkça renk değiştirir (Silikleşir).
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawWithCache {
                    onDrawWithContent {
                        drawContent() // Metinleri çiz
                        drawRect(brush = brush, blendMode = BlendMode.SrcIn) // Fırçayı uygula
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            // Padding sayesinde listenin başı ve sonu tam ortaya gelebilir
            contentPadding = PaddingValues(vertical = itemHeight)
        ) {
            items(largeCount) { index ->
                val item = items[index % items.size]

                Box(
                    modifier = Modifier
                        .height(itemHeight) // Sabit yükseklik
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        style = textStyle,
                        color = Color.Black, // Rengi Brush belirlediği için burası önemsiz
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * STATE HOLDER (DURUM YÖNETİCİSİ)
 * Saat, Dakika ve Saniye verilerini tutan sınıf.
 * Bu sayede UI (Composable) içinde logic yazmak yerine state üzerinden yönetiriz.
 */
class JustRelaxTimerState(
    initialHour: Int,
    initialMinute: Int,
    initialSecond: Int
) {
    var hour by mutableIntStateOf(initialHour)
    var minute by mutableIntStateOf(initialMinute)
    var second by mutableIntStateOf(initialSecond)

    // Toplam saniyeyi hesaplayan yardımcı özellik
    val totalSeconds: Long
        get() = (hour * 3600L) + (minute * 60L) + second

    companion object {
        // Ekran döndürüldüğünde veya tema değiştiğinde verilerin kaybolmaması için Saver.
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

// --- YARDIMCI COMPONENT ---
// Her bir tekerleği ve başlığını hizalayan sarmalayıcı
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
        // Başlık (Saat, Dakika...)
        Text(
            text = label,
            style = labelStyle,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        // Tekerlek
        content()
    }
}

@Composable
fun JustRelaxTimerPicker(
    modifier: Modifier = Modifier,
    state: JustRelaxTimerState = rememberJustRelaxTimerState()
) {
    // --- VERİLER ---
    // remember kullanıyoruz ki her çizimde listeler tekrar oluşmasın
    val hours = remember { (0..24).map { it.toString().padStart(2, '0') } }
    val minutes = remember { (0..59).map { it.toString().padStart(2, '0') } }
    val seconds = remember { (0..59).map { it.toString().padStart(2, '0') } }

    // --- STİLLER ---
    val textStyle = MaterialTheme.typography.displayLarge.copy(
        fontSize = 50.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
    )

    val labelStyle = MaterialTheme.typography.titleMedium.copy(
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
    )

    val separatorStyle = textStyle.copy(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    )

    val itemHeight = 100.dp

    // --- DÜZEN ---
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. SAAT
        TimerUnitColumn(label = "Saat", labelStyle = labelStyle) {
            InfiniteWheelPicker(
                itemHeight = itemHeight,
                items = hours,
                initialItem = state.hour.toString().padStart(2, '0'),
                textStyle = textStyle,
                onItemSelected = { _, index -> state.hour = index }
            )
        }

        // AYIRAÇ (:)
        // label = " " (Boşluk) vererek hizalamayı koruyoruz.
        TimerUnitColumn(label = " ", labelStyle = labelStyle) {
            Text(
                text = ":",
                style = separatorStyle,
                modifier = Modifier.padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 2. DAKİKA
        TimerUnitColumn(label = "Dakika", labelStyle = labelStyle) {
            InfiniteWheelPicker(
                itemHeight = itemHeight,
                items = minutes,
                initialItem = state.minute.toString().padStart(2, '0'),
                textStyle = textStyle,
                onItemSelected = { _, index -> state.minute = index }
            )
        }

        // AYIRAÇ (:)
        TimerUnitColumn(label = " ", labelStyle = labelStyle) {
            Text(
                text = ":",
                style = separatorStyle,
                modifier = Modifier.padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 3. SANİYE
        TimerUnitColumn(label = "Saniye", labelStyle = labelStyle) {
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
    // State'i burada oluşturup Picker'a veriyoruz (State Hoisting)
    val pickerState = rememberJustRelaxTimerState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Picker Bileşeni
        JustRelaxTimerPicker(
            state = pickerState
        )

        Spacer(modifier = Modifier.weight(1f))

        // Başlat Butonu
        Button(
            onClick = {
                val total = pickerState.totalSeconds
                if (total > 0) {
                    onStartClick(total)
                }
            },
            modifier = Modifier
                .widthIn(min = 120.dp) // Esnek genişlik (En az 120dp)
                .height(56.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text(
                "Başlat",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(64.dp)) // Alt boşluk (Ergonomi için)
    }
}

@Preview
@Composable
fun TimerSetupScreenPreview() {
    JustRelaxTheme {
        TimerSetupScreen({})
    }
}