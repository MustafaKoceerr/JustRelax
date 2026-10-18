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
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.settings_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onOpenCredits: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    UserMessageEffect(uiState.userMessage, viewModel::onMessageShown) { snackbarHostState.showSnackbar(it) }

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
    onUsageDataChange: (Boolean) -> Unit,
) {
    JustRelaxDetailScaffold(
        title = stringResource(Res.string.settings_title),
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
                onRateApp = onRateApp,
                onSendFeedback = onSendFeedback,
                onOpenPrivacyPolicy = onOpenPrivacyPolicy,
                onOpenTermsAndConditions = onOpenTermsAndConditions,
                onOpenCredits = onOpenCredits,
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
