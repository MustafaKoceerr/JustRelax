package com.mustafakoceerr.justrelax.feature.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.components.LoadingDots
import justrelax.feature.onboarding.generated.resources.Res
import justrelax.feature.onboarding.generated.resources.loading_config_message
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LoadingConfigView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp)
            // Enterprise: UI Testlerinin bu ekranın açıldığını anlaması için etiket
            .testTag("LoadingConfigView"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- 1. ANİMASYON ---
        // Marka kimliği: Kendi özel LoadingDots bileşenimiz
        LoadingDots(
            color = MaterialTheme.colorScheme.primary,
            dotSize = 12.dp,
            travelDistance = 8.dp
        )

        Spacer(modifier = Modifier.height(24.dp)) // 8dp grid sistemi (3x)

        // --- 2. MESAJ ---
        // Localization: Metin kaynak dosyasına taşındı.
        Text(
            text = stringResource(Res.string.loading_config_message),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
private fun LoadingConfigViewPreview() {
    // JustRelaxTheme {
    Surface {
        LoadingConfigView()
    }
    // }
}