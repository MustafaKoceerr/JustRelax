package com.mustafakoceerr.justrelax.feature.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakoceerr.justrelax.core.model.AppTheme
import com.mustafakoceerr.justrelax.core.ui.util.UserMessageEffect
import org.koin.compose.viewmodel.koinViewModel
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxSnackbarHost
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.feature.settings.components.LanguageSelectionBottomSheet
import com.mustafakoceerr.justrelax.feature.settings.components.SettingsContent
import justrelax.feature.settings.generated.resources.Res
import justrelax.feature.settings.generated.resources.settings_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
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
) {
    JustRelaxBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                JustRelaxTopBar(
                    title = stringResource(Res.string.settings_title),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                        }
                    }
                )
            },
            snackbarHost = { JustRelaxSnackbarHost(hostState = snackbarHostState) }
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
}
