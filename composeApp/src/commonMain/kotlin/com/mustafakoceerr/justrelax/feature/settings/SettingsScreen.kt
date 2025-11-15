package com.mustafakoceerr.justrelax.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.settings.domain.model.AppLanguage
import com.mustafakoceerr.justrelax.core.settings.domain.model.AppTheme
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable
data object SettingsScreen : AppScreen {

    @Composable
    override fun Content() {
        val viewModel: SettingsViewModel = koinInject()
        val theme by viewModel.currentTheme.collectAsState()
        val language by viewModel.currentLanguage.collectAsState()

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Current Theme: ${theme.name}")
            Text("Current Language: ${language.nativeName}")

            Spacer(Modifier.height(32.dp))

            Row {
                Button(onClick = { viewModel.updateTheme(AppTheme.LIGHT) }) { Text("Light") }
                Button(onClick = { viewModel.updateTheme(AppTheme.DARK) }) { Text("Dark") }
            }

            Spacer(Modifier.height(16.dp))

            Row {
                Button(onClick = { viewModel.updateLanguage(AppLanguage.ENGLISH) }) { Text("English") }
                Button(onClick = { viewModel.updateLanguage(AppLanguage.TURKISH) }) { Text("Türkçe") }
            }
        }
    }
}