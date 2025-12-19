package com.mustafakoceerr.justrelax.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mustafakoceerr.justrelax.core.navigation.AppScreen
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxSnackbarHost
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.settings.components.*
import justrelax.feature.settings.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

data object SettingsScreen : AppScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {

        val navigator = LocalNavigator.currentOrThrow
        val viewModel: SettingsViewModel = koinScreenModel()
        val snackbarController: GlobalSnackbarController = koinInject()

        // --- State'leri Dinle ---
        val currentTheme by viewModel.currentTheme.collectAsState()
        val currentLanguage by viewModel.currentLanguage.collectAsState()
        val isSheetOpen by viewModel.isLanguageSheetOpen.collectAsState()
        val isDownloading by viewModel.isDownloading.collectAsState()
        val downloadProgress by viewModel.downloadProgress.collectAsState()
        val isLibraryComplete by viewModel.isLibraryComplete.collectAsState()

        // --- Effect'leri Dinle ve Global Controller'a Yönlendir ---
        LaunchedEffect(Unit) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is SettingsEffect.ShowSnackbar -> {
                        snackbarController.showSnackbar(effect.message)
                    }
                }
            }
        }

        JustRelaxBackground {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    JustRelaxTopBar(
                        title = stringResource(Res.string.settings_title),
                        navigationIcon = {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Geri Git"
                                )
                            }
                        }
                    )

                },
                snackbarHost = { JustRelaxSnackbarHost(hostState = snackbarController.hostState) }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ThemeSelector(
                        currentTheme = currentTheme,
                        onThemeSelected = viewModel::updateTheme
                    )

                    SectionGroup(title = stringResource(Res.string.section_content)) {
                        DownloadAllCard(
                            isDownloaded = isLibraryComplete,
                            isDownloading = isDownloading,
                            progress = downloadProgress,
                            onClick = viewModel::onDownloadAllClicked
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SettingsTile(
                            icon = Icons.Rounded.Language,
                            title = stringResource(Res.string.settings_language_title),
                            subtitle = currentLanguage.nativeName,
                            onClick = viewModel::onLanguageTileClicked
                        )
                    }

                    SectionGroup(title = stringResource(Res.string.section_support)) {
                        SettingsTile(
                            icon = Icons.Rounded.StarRate,
                            title = stringResource(Res.string.rate_app_title),
                            subtitle = stringResource(Res.string.rate_app_subtitle),
                            onClick = viewModel::rateApp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        val emailSubject = stringResource(Res.string.feedback_subject)
                        val emailBody = stringResource(Res.string.feedback_body_template)
                        val targetEmail = stringResource(Res.string.support_email)
                        SettingsTile(
                            icon = Icons.Rounded.Mail,
                            title = stringResource(Res.string.feedback_title),
                            subtitle = stringResource(Res.string.feedback_subtitle),
                            onClick = {
                                viewModel.sendFeedback(
                                    targetEmail,
                                    emailSubject,
                                    emailBody
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        val privacyUrl = stringResource(Res.string.privacy_policy_url)
                        SettingsTile(
                            icon = Icons.Rounded.Info,
                            title = stringResource(Res.string.about_title),
                            subtitle = "v1.0.0", // TODO: BuildKonfig'den dinamik çek
                            onClick = { viewModel.openPrivacyPolicy(privacyUrl) }
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            if (isSheetOpen) {
                LanguageSelectionBottomSheet(
                    availableLanguages = AppLanguage.entries,
                    currentLanguageCode = currentLanguage.code,
                    onDismissRequest = viewModel::dismissLanguageSheet,
                    onLanguageSelected = viewModel::onLanguageSelected
                )
            }
        }
    }

    @Composable
    private fun SectionGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
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