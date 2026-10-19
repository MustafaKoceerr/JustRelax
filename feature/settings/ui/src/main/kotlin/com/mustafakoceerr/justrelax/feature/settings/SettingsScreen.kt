package com.mustafakoceerr.justrelax.feature.settings

import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakoceerr.justrelax.core.model.AppTheme
import com.mustafakoceerr.justrelax.core.ui.util.UserMessageEffect
import org.koin.compose.viewmodel.koinViewModel
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxDetailScaffold
import com.mustafakoceerr.justrelax.feature.settings.components.LanguageSelectionBottomSheet
import com.mustafakoceerr.justrelax.feature.settings.components.SettingsContent
import com.mustafakoceerr.justrelax.feature.settings.ui.R
import androidx.compose.ui.res.stringResource

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onOpenCredits: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenMySounds: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    UserMessageEffect(uiState.userMessage?.text(), viewModel::onMessageShown) { snackbarHostState.showSnackbar(it) }

    SettingsScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onThemeChange = viewModel::changeTheme,
        onDownloadLibrary = viewModel::downloadLibrary,
        onOpenLanguageSelection = viewModel::openLanguageSelection,
        onCloseLanguageSelection = viewModel::closeLanguageSelection,
        onLanguageChange = viewModel::changeLanguage,
        onRateApp = viewModel::rateApp,
        onSendFeedback = viewModel::sendFeedback,
        onOpenPrivacyPolicy = viewModel::openPrivacyPolicy,
        onOpenTermsAndConditions = viewModel::openTermsAndConditions,
        onOpenCredits = onOpenCredits,
        onOpenSupport = onOpenSupport,
        onOpenMySounds = onOpenMySounds,
        onUsageDataChange = viewModel::setUsageDataEnabled,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onDownloadLibrary: () -> Unit,
    onOpenLanguageSelection: () -> Unit,
    onCloseLanguageSelection: () -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onRateApp: () -> Unit,
    onSendFeedback: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTermsAndConditions: () -> Unit,
    onOpenCredits: () -> Unit,
    onOpenSupport: () -> Unit,
    onOpenMySounds: () -> Unit,
    onUsageDataChange: (Boolean) -> Unit,
) {
    JustRelaxDetailScaffold(
        title = stringResource(R.string.settings_title),
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            SettingsContent(
                uiState = uiState,
                onThemeChange = onThemeChange,
                onDownloadLibrary = onDownloadLibrary,
                onOpenLanguageSelection = onOpenLanguageSelection,
                onOpenMySounds = onOpenMySounds,
                onRateApp = onRateApp,
                onSendFeedback = onSendFeedback,
                onOpenPrivacyPolicy = onOpenPrivacyPolicy,
                onOpenTermsAndConditions = onOpenTermsAndConditions,
                onOpenCredits = onOpenCredits,
                onOpenSupport = onOpenSupport,
                onUsageDataChange = onUsageDataChange,
            )

            if (uiState.isLanguageSheetOpen) {
                LanguageSelectionBottomSheet(
                    availableLanguages = AppLanguage.entries,
                    currentLanguageCode = uiState.language.code,
                    onDismissRequest = onCloseLanguageSelection,
                    onLanguageSelected = onLanguageChange,
                )
            }
        }
    }
}

@Composable
private fun SettingsMessage.text(): String = when (this) {
    SettingsMessage.LibraryDownloaded -> stringResource(R.string.msg_all_sounds_downloaded)
    is SettingsMessage.Text -> value
}
