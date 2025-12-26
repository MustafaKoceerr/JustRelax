package com.mustafakoceerr.justrelax.feature.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.StarRate
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.feature.settings.mvi.SettingsIntent
import com.mustafakoceerr.justrelax.feature.settings.mvi.SettingsState
import justrelax.feature.settings.generated.resources.Res
import justrelax.feature.settings.generated.resources.feedback_subtitle
import justrelax.feature.settings.generated.resources.feedback_title
import justrelax.feature.settings.generated.resources.privacy_policy_title
import justrelax.feature.settings.generated.resources.rate_app_subtitle
import justrelax.feature.settings.generated.resources.rate_app_title
import justrelax.feature.settings.generated.resources.section_content
import justrelax.feature.settings.generated.resources.section_support_legal
import justrelax.feature.settings.generated.resources.settings_language_title
import justrelax.feature.settings.generated.resources.terms_conditions_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsContent(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        // Ana bölümler arasındaki boşluk
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. TEMA SEÇİMİ
        ThemeSelector(
            currentTheme = state.currentTheme,
            onThemeSelected = { onIntent(SettingsIntent.ChangeTheme(it)) }
        )

        // 2. İÇERİK (İndirme & Dil)
        // İYİLEŞTİRME: İçindeki Spacer'lar kaldırıldı.
        SectionGroup(title = stringResource(Res.string.section_content)) {
            DownloadAllCard(
                isDownloaded = state.isLibraryComplete,
                isDownloading = state.isDownloadingLibrary,
                progress = state.downloadProgress,
                onClick = { onIntent(SettingsIntent.DownloadAllLibrary) }
            )
            SettingsTile(
                icon = Icons.Rounded.Language,
                title = stringResource(Res.string.settings_language_title),
                subtitle = state.currentLanguage.nativeName,
                onClick = { onIntent(SettingsIntent.OpenLanguageSelection) }
            )
        }

        // 3. DESTEK VE YASAL (GÜNCELLENDİ)
        SectionGroup(title = stringResource(Res.string.section_support_legal)) {
            // Destek Kısmı
            SettingsTile(
                icon = Icons.Rounded.StarRate,
                title = stringResource(Res.string.rate_app_title),
                subtitle = stringResource(Res.string.rate_app_subtitle),
                onClick = { onIntent(SettingsIntent.RateApp) }
            )
            SettingsTile(
                icon = Icons.Rounded.Mail,
                title = stringResource(Res.string.feedback_title),
                subtitle = stringResource(Res.string.feedback_subtitle),
                onClick = { onIntent(SettingsIntent.SendFeedback) }
            )

            // Görsel ayrım için ince bir çizgi (Opsiyonel ama şık durur)
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Yasal Kısmı
            SettingsTile(
                icon = Icons.Rounded.PrivacyTip, // Gizlilik için uygun ikon
                title = stringResource(Res.string.privacy_policy_title),
                onClick = { onIntent(SettingsIntent.OpenPrivacyPolicy) }
            )
            SettingsTile(
                icon = Icons.Rounded.Description, // Terms için belge ikonu
                title = stringResource(Res.string.terms_conditions_title),
                onClick = { onIntent(SettingsIntent.OpenTermsAndConditions) }
            )

            // Versiyon Bilgisi (Tıklanabilir değil, sadece bilgi)
            VersionInfoTile(versionName = "v1.0.0")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Versiyon bilgisini pasif bir şekilde gösteren özel bir tile.
 * Tıklanabilir olmasına gerek yok, sadece bilgi verir.
 */
@Composable
private fun VersionInfoTile(versionName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Just Relax $versionName",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

/**
 * Başlığı olan ve içeriğindeki elemanların arasına otomatik boşluk ekleyen
 * akıllı bir bölüm grubu.
 */
@Composable
private fun SectionGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        // İYİLEŞTİRME: Bu iç Column, çocukları arasına 12.dp boşluk ekler.
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content()
        }
    }
}