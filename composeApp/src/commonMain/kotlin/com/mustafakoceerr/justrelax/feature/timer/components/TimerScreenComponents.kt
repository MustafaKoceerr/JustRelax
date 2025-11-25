package com.mustafakoceerr.justrelax.feature.timer.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TimerScreen(){
    // Bu kutu kendisine ayrılan alanın genişliğini ve yüksekliğini bilir.
    BoxWithConstraints {
        val isLandscape = maxWidth > maxHeight

        if (isLandscape){
            // Yatay tasarım
            TimerLandscapeLayout()

        }else{
            // Dikey tasarım
            TimerPortraitLayout(1,1,{},{})
        }
    }
}
