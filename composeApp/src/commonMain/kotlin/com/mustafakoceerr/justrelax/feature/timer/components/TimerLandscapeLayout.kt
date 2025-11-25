package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import com.mustafakoceerr.justrelax.feature.timer.TimerViewModel
import io.ktor.utils.io.ioDispatcher
import org.jetbrains.compose.ui.tooling.preview.Preview



@Composable
fun TimerTextDisplay(
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier, // dışarıdan weight alacak.
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "3 sa 6 dk 13 sn",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
            )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Satır: Kocaman sayaç
        Text(
            text = "03 : 06 : 08",
            // Yatay modda ekran geniş olduğu için fontu iyice büyütebiliriz
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Satır: Zil ikonu ve bitiş saati
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Notifications,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "00:18",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TimerButtonsRow(
    onPauseClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp), // En alttan biraz boşluk bırakalım.
        horizontalArrangement = Arrangement.Center, // Ortaya topla
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sol button: Sil (İkincil aksiyon -> Tonal)
        FilledTonalButton(
            onClick = onCancelClick,
            modifier = Modifier
                .height(50.dp)
                .width(110.dp) // Genişlik verelim ki şık dursun.
        ){
            Icon(Icons.Rounded.Close, null, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Sil", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.width(32.dp)) // İki buton arası boşluk

        Button(
            onClick = onPauseClick,
            modifier = Modifier
                .height(50.dp)
                .width(130.dp), // Ana button bir tık daha geniş olabilir.
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ){
            Icon(Icons.Rounded.Pause,null, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Duraklat", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun TimerLandscapeLayout(){

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. METİN KISMI
        // weight(1f) diyerek "Ekranın boş kalan TÜM alanını sen kapla" diyoruz.
        // Böylece yazılar ekranın ortasına gelir, butonları aşağı iter.
        TimerTextDisplay(
            modifier = Modifier.weight(1f)
        )

        // 2. BUTON KISMI
        // Buna weight vermiyoruz, sadece ihtiyacı kadar yer kaplıyor.
        TimerButtonsRow(
            onPauseClick = {},
            onCancelClick = {}
        )
    }
}

@Preview
@Composable
fun TimerLandscapeLayoutPreview(){
    JustRelaxTheme {
        TimerLandscapeLayout()
    }
}

@Preview
@Composable
fun TimerButtonsRowPreview(){
    JustRelaxTheme {
        TimerButtonsRow(
            {},
            {}
        )
    }
}
@Preview
@Composable
fun TimerTextDisplayPreview(){
    JustRelaxTheme {
        TimerTextDisplay()
    }
}
