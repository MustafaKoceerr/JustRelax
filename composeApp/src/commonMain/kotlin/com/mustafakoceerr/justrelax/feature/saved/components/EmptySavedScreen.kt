package com.mustafakoceerr.justrelax.feature.saved.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mustafakoceerr.justrelax.composeapp.generated.resources.Res
import com.mustafakoceerr.justrelax.composeapp.generated.resources.saved_empty_vector
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SavedMixesEmptyState(
    onCreateClick: () -> Unit, // Kullanıcıyı Mixer ekranına yönlendirecek fonksiyon
    modifier: Modifier = Modifier
) {

}

@Composable
fun EmptyStateVisual(
    modifier: Modifier = Modifier
) {
    // --- SÜZÜLME ANİMASYONU (FLOATING) ---
    // Vektörün canlı görünmesi için hafif yukarı-aşağı hareketi
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing), // Yavaş ve akıcı
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // KMP Kaynağı Kullanımı
        Image(
            painter = painterResource(Res.drawable.saved_empty_vector),
            contentDescription = "Henüz kaydedilmiş mix yok",
            modifier = Modifier
                .height(220.dp) // Vektörün boyutuna göre burayı artırıp azaltabilirsin
                .fillMaxWidth()
                // Animasyonu uyguluyoruz:
                .graphicsLayer { translationY = offsetY },
            contentScale = ContentScale.Fit
        )
    }
}

@Preview
@Composable
fun EmptyStateVisualPreview(){
    JustRelaxTheme {
        EmptyStateVisual()
    }
}

@Composable
fun EmptyStateMessage(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Başlık
        Text(
            text = "Henüz Kayıt Yok",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Açıklama
        Text(
            text = "Favori seslerini karıştırıp buraya kaydedebilirsin. Kendi huzur koleksiyonunu oluşturmaya başla.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp // Okunabilirlik için satır aralığı
        )
    }
}

@Preview
@Composable
fun EmptyStateMessagePreview(){
    JustRelaxTheme {
        EmptyStateMessage()
    }

}

@Composable
fun EmptyStateAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .widthIn(min = 200.dp) // En az 200dp genişlik
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Icon(Icons.Rounded.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Yeni Mix Oluştur",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Preview
@Composable
fun EmptyStateActionPreview(

) {
    JustRelaxTheme {
        EmptyStateAction(
            {}
        )
    }

}



@Composable
fun SavedMixesEmptyScreen(
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp), // Genel kenar boşluğu
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Dikeyde ortala
    ) {
        // 1. Görsel
        EmptyStateVisual()

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Mesaj
        EmptyStateMessage()

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Aksiyon
        EmptyStateAction(
            onClick = onCreateClick
        )
    }
}

@Preview
@Composable
fun SavedMixesEmptyScreenPreview(
) {
    JustRelaxTheme {
        SavedMixesEmptyScreen(
            {}
        )
    }

}