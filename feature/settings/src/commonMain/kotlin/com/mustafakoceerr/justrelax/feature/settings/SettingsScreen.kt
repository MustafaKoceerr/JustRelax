package com.mustafakoceerr.justrelax.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.StarRate
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.feature.settings.components.DownloadAllCard
import com.mustafakoceerr.justrelax.feature.settings.components.SettingsTile
import com.mustafakoceerr.justrelax.feature.settings.components.ThemeSelector
import com.mustafakoceerr.justrelax.feature.settings.util.nativeName
import org.koin.compose.viewmodel.koinViewModel

data object SettingsScreen : AppScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel: SettingsViewModel = koinViewModel()

        // State'leri dinle
        val currentTheme by viewModel.currentTheme.collectAsState()
        val currentLanguage by viewModel.currentLanguage.collectAsState()

        // TODO: İndirme durumu ileride ViewModel'den gelebilir
        val isDownloading = false
        val downloadProgress = 0f
        val isAllDownloaded = false

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Ayarlar") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // 1. GÖRÜNÜM BÖLÜMÜ
                ThemeSelector(
                    currentTheme = currentTheme,
                    onThemeSelected = { newTheme ->
                        viewModel.updateTheme(newTheme)
                    }
                )

                // 2. İÇERİK BÖLÜMÜ
                SectionGroup(title = "İçerik") {
                    // Özel İndirme Kartı (Şimdilik Mock)
                    DownloadAllCard(
                        isDownloaded = isAllDownloaded,
                        isDownloading = isDownloading,
                        progress = downloadProgress,
                        onClick = { /* İndirme Logic'i eklenecek */ }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dil Seçimi
                    SettingsTile(
                        icon = Icons.Rounded.Language,
                        title = "Dil / Language",
                        // nativeName extension'ı ile "Türkçe" veya "English" yazar
                        subtitle = currentLanguage.nativeName,
                        onClick = {
                            viewModel.openSettings() // Sistem ayarlarına yönlendir
                        }
                    )
                }

                // 3. DESTEK BÖLÜMÜ
                SectionGroup(title = "Destek") {
                    SettingsTile(
                        icon = Icons.Rounded.StarRate,
                        title = "Uygulamayı Puanla",
                        subtitle = "Bize destek ol",
                        onClick = { /* Google Play Intent - Sonra eklenecek */ }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsTile(
                        icon = Icons.Rounded.Mail,
                        title = "İletişim & Hata Bildir",
                        subtitle = "Bir sorun mu var?",
                        onClick = { /* Email Intent - Sonra eklenecek */ }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsTile(
                        icon = Icons.Rounded.Info,
                        title = "Hakkında",
                        subtitle = "v1.0.0",
                        onClick = { /* Lisanslar vs. - Sonra eklenecek */ }
                    )
                }

                // Alt boşluk
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}


@Composable
private fun SectionGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        content()
    }
}