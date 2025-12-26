package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Uygulamanın genelinde kullanılacak Snackbar Host.
 * Scaffold'un snackbarHost parametresine bu verilmeli.
 */
@Composable
fun JustRelaxSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier // Best Practice: Dışarıdan müdahale edilebilir olmalı
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { data ->
        // Veriyi UI bileşenine dönüştürüyoruz (Adapter Pattern)
        JustRelaxSnackbar(
            message = data.visuals.message,
            actionLabel = data.visuals.actionLabel,
            onActionClick = { data.performAction() },
            onDismiss = { data.dismiss() }
        )
    }
}

/**
 * Saf UI Bileşeni (Stateless).
 * SnackbarData yerine String alır, böylece Preview ve Test edilebilir.
 * Private veya Internal olabilir, çünkü genelde sadece Host içinden çağrılır.
 */
@Composable
private fun JustRelaxSnackbar(
    message: String,
    actionLabel: String?,
    onActionClick: () -> Unit,
    onDismiss: () -> Unit, // Gerekirse dismiss aksiyonu için (Swipe desteği Host'tadır ama buton eklenebilir)
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(16.dp) // Ekran kenarlarından boşluk
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp), // PDF Kuralı: Standart shape kullanımı
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        shadowElevation = 6.dp,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp), // İçerik boşluğu
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Mesaj
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )

            // Aksiyon Butonu (Varsa)
            if (actionLabel != null) {
                Spacer(modifier = Modifier.width(16.dp))
                TextButton(
                    onClick = onActionClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.inversePrimary
                    )
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

// --- PREVIEW ---
// Artık Snackbar'ı host olmadan, data mocklamadan görebiliriz.

@Preview
@Composable
private fun JustRelaxSnackbarPreview() {
    MaterialTheme {
        JustRelaxSnackbar(
            message = "İndirme işlemi tamamlandı.",
            actionLabel = "GÖRÜNTÜLE",
            onActionClick = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun JustRelaxSnackbarNoActionPreview() {
    MaterialTheme {
        JustRelaxSnackbar(
            message = "Bağlantı hatası oluştu.",
            actionLabel = null,
            onActionClick = {},
            onDismiss = {}
        )
    }
}