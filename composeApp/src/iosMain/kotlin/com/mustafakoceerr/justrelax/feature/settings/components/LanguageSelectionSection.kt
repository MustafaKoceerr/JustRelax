package com.mustafakoceerr.justrelax.feature.settings.components


import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.feature.settings.SettingsViewModel

@Composable
actual fun LanguageSelectionSection(viewModel: SettingsViewModel) {
    // IOS: Ayarlara yönlendirme butonu
    Text("To change language, please go to system settings.")
    Spacer(Modifier.height(8.dp))

    Button(onClick = { viewModel.openSettings() }) {
        Text("Open Settings")
    }
}