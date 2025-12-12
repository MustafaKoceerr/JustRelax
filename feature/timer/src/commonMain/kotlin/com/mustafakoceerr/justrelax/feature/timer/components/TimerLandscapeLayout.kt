package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mustafakoceerr.justrelax.core.audio.TimerStatus
import com.mustafakoceerr.justrelax.core.common.formatTime
import com.mustafakoceerr.justrelax.feature.timer.util.calculateEndTime
import com.mustafakoceerr.justrelax.feature.timer.util.formatDurationVerbose


@Composable
fun TimerTextDisplay(
    totalTimeSeconds: Long,
    timeLeftSeconds: Long,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Satır: Toplam Ayarlanan Süre (Örn: 3 sa 6 dk 13 sn)
        Text(
            text = formatDurationVerbose(totalTimeSeconds),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Satır: Kocaman Sayaç (Örn: 03 : 06 : 08)
        Text(
            text = formatTime(timeLeftSeconds),
            // Yatay modda ekran geniş olduğu için fontu iyice büyütebiliriz
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Satır: Zil ikonu ve Bitiş Saati (Dinamik)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Notifications,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))

            // DİNAMİK BİTİŞ SAATİ
            Text(
                text = calculateEndTime(timeLeftSeconds),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TimerButtonsRow(
    status: TimerStatus,
    onToggleClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            // Alttan boşluk
            .padding(bottom = 24.dp),
        // Eğer ekran çok darsa butonlar birbirine girmesin, araları açılsın
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- SOL BUTON: SİL ---
        FilledTonalButton(
            onClick = onCancelClick,
            modifier = Modifier
                .height(50.dp)
                // DEFENSIVE 1: Sabit width yerine widthIn.
                // En az 100dp olsun ama gerekirse uzasın.
                .widthIn(min = 100.dp),
            contentPadding = PaddingValues(horizontal = 16.dp) // İç boşluk
        ) {
            Icon(Icons.Rounded.Close, null, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Sil",
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(32.dp)) // Aradaki boşluk (Biraz kıstım)

        // --- SAĞ BUTON: DURAKLAT / DEVAM ET ---
        val isRunning = status == TimerStatus.RUNNING
        Button(
            onClick = onToggleClick,
            modifier = Modifier
                .height(50.dp)
                // DEFENSIVE 2: Sabit 140dp yerine widthIn.
                // "Devam Et" uzun bir yazı, o yüzden min 130dp verelim, sığmazsa uzasın.
                .widthIn(min = 130.dp),
            contentPadding = PaddingValues(horizontal = 24.dp), // Yazı kenara yapışmasın
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                null, Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))

            // Metin uzarsa butonu itecek, sığmazsa ... koyacak (Son çare)
            Text(
                text = if (isRunning) "Duraklat" else "Devam Et",
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TimerLandscapeLayout(
    totalTimeSeconds: Long,
    timeLeftSeconds: Long,
    status: TimerStatus,
    onToggleClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. METİN KISMI
        TimerTextDisplay(
            totalTimeSeconds = totalTimeSeconds,
            timeLeftSeconds = timeLeftSeconds, // Parametre ismi düzeltildi
            modifier = Modifier.weight(1f)
        )

        // 2. BUTON KISMI
        TimerButtonsRow(
            status = status,
            onToggleClick = onToggleClick,
            onCancelClick = onCancelClick
        )
    }
}