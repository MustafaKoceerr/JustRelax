package com.mustafakoceerr.justrelax.feature.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.composeapp.generated.resources.Res
import com.mustafakoceerr.justrelax.composeapp.generated.resources.language_english
import com.mustafakoceerr.justrelax.composeapp.generated.resources.language_turkish
import com.mustafakoceerr.justrelax.core.settings.domain.model.AppLanguage
import com.mustafakoceerr.justrelax.feature.settings.SettingsViewModel

import org.jetbrains.compose.resources.stringResource

@Composable
actual fun LanguageSelectionSection(viewModel: SettingsViewModel) {
    // ANDROID: Uygulama içi değiştirme butonları
    Button(onClick = { viewModel.updateLanguage(AppLanguage.SYSTEM) }) {
        Text("System Default")
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { viewModel.updateLanguage(AppLanguage.ENGLISH) }) {
            Text(stringResource(Res.string.language_english))
        }
        Button(onClick = { viewModel.updateLanguage(AppLanguage.TURKISH) }) {
            Text(stringResource(Res.string.language_turkish))
        }
    }
}