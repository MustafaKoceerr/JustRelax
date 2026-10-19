package com.mustafakoceerr.justrelax.feature.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material.icons.rounded.StarRate
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.model.AppTheme
import com.mustafakoceerr.justrelax.core.ui.components.AppMark
import com.mustafakoceerr.justrelax.core.ui.util.WindowWidthSize
import com.mustafakoceerr.justrelax.core.ui.util.rememberWindowSizeClass
import com.mustafakoceerr.justrelax.feature.settings.SettingsUiState
import com.mustafakoceerr.justrelax.feature.settings.ui.R
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource

/**
 * The settings as an app card and four groups: appearance, sounds, privacy and support. On
 * expanded windows the groups split into two columns; otherwise they stack at a readable width.
 */
@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    onThemeChange: (AppTheme) -> Unit,
    onDownloadLibrary: () -> Unit,
    onOpenLanguageSelection: () -> Unit,
    onOpenMySounds: () -> Unit,
    onRateApp: () -> Unit,
    onSendFeedback: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTermsAndConditions: () -> Unit,
    onOpenCredits: () -> Unit,
    onOpenSupport: () -> Unit,
    onUsageDataChange: (Boolean) -> Unit,
) {
    val appearance = @Composable {
        SettingsGroup(title = stringResource(R.string.theme_section_title)) {
            row {
                SettingsRow(icon = Icons.Rounded.Palette, title = stringResource(R.string.settings_theme_title), onClick = null, trailing = {}) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ThemeChoice(current = uiState.theme, onSelect = onThemeChange)
                }
            }
            row {
                SettingsRow(
                    icon = Icons.Rounded.Language,
                    title = stringResource(R.string.settings_language_title),
                    subtitle = uiState.language.nativeName,
                    onClick = onOpenLanguageSelection,
                )
            }
        }
    }
    val sounds = @Composable {
        SettingsGroup(title = stringResource(R.string.section_sounds)) {
            row { DownloadAllRow(uiState = uiState, onDownload = onDownloadLibrary) }
            row {
                SettingsRow(
                    icon = Icons.Rounded.LibraryMusic,
                    title = stringResource(R.string.settings_my_sounds_title),
                    subtitle = stringResource(R.string.settings_my_sounds_subtitle),
                    onClick = onOpenMySounds,
                )
            }
        }
    }
    val privacy = @Composable {
        SettingsGroup(title = stringResource(R.string.section_privacy)) {
            row {
                SettingsRow(
                    icon = Icons.Rounded.Insights,
                    title = stringResource(R.string.settings_usage_data_title),
                    subtitle = stringResource(R.string.settings_usage_data_subtitle),
                    onClick = { onUsageDataChange(!uiState.isUsageDataEnabled) },
                    trailing = { Switch(checked = uiState.isUsageDataEnabled, onCheckedChange = onUsageDataChange) },
                )
            }
            row { SettingsRow(icon = Icons.Rounded.PrivacyTip, title = stringResource(R.string.privacy_policy_title), onClick = onOpenPrivacyPolicy) }
            row { SettingsRow(icon = Icons.Rounded.Description, title = stringResource(R.string.terms_conditions_title), onClick = onOpenTermsAndConditions) }
        }
    }
    val support = @Composable {
        SettingsGroup(title = stringResource(R.string.section_support)) {
            if (uiState.canTip) {
                row {
                    SettingsRow(
                        icon = Icons.Rounded.LocalCafe,
                        title = stringResource(R.string.settings_support_title),
                        subtitle = stringResource(R.string.settings_support_subtitle),
                        onClick = onOpenSupport,
                    )
                }
            }
            row {
                SettingsRow(
                    icon = Icons.Rounded.StarRate,
                    title = stringResource(R.string.rate_app_title),
                    subtitle = stringResource(R.string.rate_app_subtitle),
                    onClick = onRateApp,
                )
            }
            row {
                SettingsRow(
                    icon = Icons.Rounded.Mail,
                    title = stringResource(R.string.feedback_title),
                    subtitle = stringResource(R.string.feedback_subtitle),
                    onClick = onSendFeedback,
                )
            }
            row {
                SettingsRow(
                    icon = Icons.Rounded.Favorite,
                    title = stringResource(R.string.credits_title),
                    subtitle = stringResource(R.string.credits_subtitle),
                    onClick = onOpenCredits,
                )
            }
        }
    }

    val isExpanded = rememberWindowSizeClass() == WindowWidthSize.EXPANDED
    Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = if (isExpanded) EXPANDED_MAX_WIDTH else READABLE_MAX_WIDTH)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            AppCard(versionName = uiState.versionName)
            if (isExpanded) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        appearance()
                        sounds()
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        privacy()
                        support()
                    }
                }
            } else {
                appearance()
                sounds()
                privacy()
                support()
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/** The app's name, what it is for and the installed version. */
@Composable
private fun AppCard(versionName: String) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            AppMark()
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "Just Relax", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = stringResource(R.string.settings_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (versionName.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.settings_version, versionName),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeChoice(current: AppTheme, onSelect: (AppTheme) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        AppTheme.entries.forEachIndexed { index, theme ->
            SegmentedButton(
                selected = theme == current,
                onClick = { onSelect(theme) },
                shape = SegmentedButtonDefaults.itemShape(index, AppTheme.entries.size),
                icon = { Icon(theme.icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                label = { Text(stringResource(theme.label), maxLines = 1) },
            )
        }
    }
}

/** Downloads every sound: a tap starts it, the row shows its progress while it runs and a check when done. */
@Composable
private fun DownloadAllRow(uiState: SettingsUiState, onDownload: () -> Unit) {
    val isDone = uiState.isLibraryComplete
    val isRunning = uiState.isDownloadingLibrary
    SettingsRow(
        icon = if (isDone) Icons.Rounded.CheckCircle else Icons.Rounded.CloudDownload,
        title = stringResource(if (isDone) R.string.download_all_completed_title else R.string.download_all_title),
        subtitle = if (isDone) null else stringResource(R.string.download_all_subtitle_recommended),
        onClick = if (isDone || isRunning) null else onDownload,
        trailing = {
            Crossfade(targetState = isDone, label = "DownloadTrailing") { done ->
                if (done) Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        },
    ) {
        AnimatedVisibility(visible = isRunning, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                LinearProgressIndicator(
                    progress = { uiState.downloadProgress },
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(R.string.download_progress, (uiState.downloadProgress * 100).toInt()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                )
            }
        }
    }
}

@get:StringRes
private val AppTheme.label: Int
    get() = when (this) {
        AppTheme.SYSTEM -> R.string.theme_system
        AppTheme.LIGHT -> R.string.theme_light
        AppTheme.DARK -> R.string.theme_dark
    }

private val AppTheme.icon: ImageVector
    get() = when (this) {
        AppTheme.SYSTEM -> Icons.Rounded.SettingsBrightness
        AppTheme.LIGHT -> Icons.Rounded.LightMode
        AppTheme.DARK -> Icons.Rounded.DarkMode
    }

private val READABLE_MAX_WIDTH = 640.dp
private val EXPANDED_MAX_WIDTH = 1040.dp
