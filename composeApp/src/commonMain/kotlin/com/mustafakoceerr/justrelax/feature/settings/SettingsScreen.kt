package com.mustafakoceerr.justrelax.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.composeapp.generated.resources.Res
import com.mustafakoceerr.justrelax.composeapp.generated.resources.*
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.settings.domain.model.AppTheme
import com.mustafakoceerr.justrelax.feature.settings.components.LanguageSelectionSection
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object SettingsScreen : AppScreen {

    @Composable
    override fun Content() {
        val viewModel: SettingsViewModel = koinViewModel()
        val theme by viewModel.currentTheme.collectAsState()
        val language by viewModel.currentLanguage.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Mevcut Tema
            Text(stringResource(Res.string.current_theme, theme.name))

            // Mevcut Dil
            Text(stringResource(Res.string.current_language, language.nativeName))

            Spacer(Modifier.height(32.dp))

            // TEMA BUTONLARI
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.updateTheme(AppTheme.LIGHT) }) {
                    Text(stringResource(Res.string.theme_light))
                }
                Button(onClick = { viewModel.updateTheme(AppTheme.DARK) }) {
                    Text(stringResource(Res.string.theme_dark))
                }
                Button(onClick = { viewModel.updateTheme(AppTheme.SYSTEM) }) {
                    Text(stringResource(Res.string.theme_system))
                }
            }

            Spacer(Modifier.height(16.dp))

            // DİL
            // System butonu eklendi
            // DİL (Platforma göre değişen parça)
            Text("Language Settings")

            // İşte burası! if-else yok, temiz.
            LanguageSelectionSection(viewModel)
        }
    }
}
