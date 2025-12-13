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
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.feature.settings.components.DownloadAllCard
import com.mustafakoceerr.justrelax.feature.settings.components.LanguageSelectionBottomSheet
import com.mustafakoceerr.justrelax.feature.settings.components.SettingsTile
import com.mustafakoceerr.justrelax.feature.settings.components.ThemeSelector
import justrelax.feature.settings.generated.resources.Res
import justrelax.feature.settings.generated.resources.about_title
import justrelax.feature.settings.generated.resources.feedback_body_template
import justrelax.feature.settings.generated.resources.feedback_subject
import justrelax.feature.settings.generated.resources.feedback_subtitle
import justrelax.feature.settings.generated.resources.feedback_title
import justrelax.feature.settings.generated.resources.privacy_policy_url
import justrelax.feature.settings.generated.resources.rate_app_subtitle
import justrelax.feature.settings.generated.resources.rate_app_title
import justrelax.feature.settings.generated.resources.section_content
import justrelax.feature.settings.generated.resources.section_support
import justrelax.feature.settings.generated.resources.settings_language_title
import justrelax.feature.settings.generated.resources.settings_title
import justrelax.feature.settings.generated.resources.support_email
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

data object SettingsScreen : AppScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel: SettingsViewModel = koinViewModel()

        // State'leri dinle
        val currentTheme by viewModel.currentTheme.collectAsState()
        val currentLanguage by viewModel.currentLanguage.collectAsState()
        // BottomSheet State'ini dinle
        val isSheetOpen by viewModel.isLanguageSheetOpen.collectAsState()

        // TODO: İndirme durumu ileride ViewModel'den gelebilir
        val isDownloading = false
        val downloadProgress = 0f
        val isAllDownloaded = false

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(Res.string.settings_title)) },
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
                SectionGroup(title = stringResource(Res.string.section_content)) {
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
                        title = stringResource(Res.string.settings_language_title),
                        // nativeName extension'ı ile "Türkçe" veya "English" yazar
                        subtitle = currentLanguage.nativeName,
                        onClick = {
                            // Tıklanınca ViewModel karar verecek (Sheet mi, Ayarlar mı?)
                            viewModel.onLanguageTileClicked()                         }
                    )
                }

                // 3. DESTEK BÖLÜMÜ
                SectionGroup(title = stringResource(Res.string.section_support)) {
                    SettingsTile(
                        icon = Icons.Rounded.StarRate,
                        title = stringResource(Res.string.rate_app_title),
                        subtitle = stringResource(Res.string.rate_app_subtitle),
                        onClick = { viewModel.rateApp() }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stringleri UI'da çözümlüyoruz (Localization burada çalışır)
                    val emailSubject = stringResource(Res.string.feedback_subject)
                    val emailBody = stringResource(Res.string.feedback_body_template)
                    val targetEmail = stringResource(Res.string.support_email)

                    SettingsTile(
                        icon = Icons.Rounded.Mail,
                        title = stringResource(Res.string.feedback_title),
                        subtitle = stringResource(Res.string.feedback_subtitle),
                        onClick = {
                            // ViewModel'e hazır stringleri atıyoruz
                            viewModel.sendFeedback(
                                supportEmail = targetEmail,
                                subject = emailSubject,
                                body = emailBody
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val privacyUrl = stringResource(Res.string.privacy_policy_url)
                    SettingsTile(
                        icon = Icons.Rounded.Info,
                        title = stringResource(Res.string.about_title),
                        subtitle = "v1.0.0", // Bunu ileride dinamik yapacağız
                        onClick = {
                            viewModel.openPrivacyPolicy(privacyUrl)
                        }
                    )
                }

                // Alt boşluk
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Bottom Sheet Gösterimi
        if (isSheetOpen) {
            LanguageSelectionBottomSheet(
                availableLanguages = AppLanguage.entries, // Direkt Enum listesi
                currentLanguageCode = currentLanguage.code,
                onDismissRequest = { viewModel.dismissLanguageSheet() },
                onLanguageSelected = { selectedLanguage ->
                    viewModel.onLanguageSelected(selectedLanguage)
                }
            )
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
}