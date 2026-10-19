package com.mustafakoceerr.justrelax.feature.library

import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.core.ui.util.UserMessageEffect
import org.koin.compose.viewmodel.koinViewModel
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTabScaffold
import com.mustafakoceerr.justrelax.core.ui.components.rememberVolumeOf
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.library.components.LibraryContent
import com.mustafakoceerr.justrelax.feature.library.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.library.generated.resources.action_my_sounds
import com.mustafakoceerr.justrelax.feature.library.generated.resources.action_settings
import com.mustafakoceerr.justrelax.feature.library.generated.resources.download_failed
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun LibraryRoute(
    onOpenSettings: () -> Unit,
    onOpenMySounds: () -> Unit,
    viewModel: LibraryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val volumeOf = rememberVolumeOf(viewModel.volumes.collectAsStateWithLifecycle())
    val snackbarController = koinInject<GlobalSnackbarController>()

    UserMessageEffect(uiState.userMessage?.text(), viewModel::onMessageShown) { snackbarController.showSnackbar(it) }

    LibraryScreen(
        uiState = uiState,
        volumeOf = volumeOf,
        onOpenSettings = onOpenSettings,
        onOpenMySounds = onOpenMySounds,
        onCategorySelected = viewModel::selectCategory,
        onSoundClick = viewModel::toggleSound,
        onVolumeChange = viewModel::changeVolume,
        onFrequencyChange = viewModel::changeFrequency,
        onRetry = viewModel::retry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    uiState: LibraryUiState,
    volumeOf: (soundId: String) -> Float,
    onOpenSettings: () -> Unit,
    onOpenMySounds: () -> Unit,
    onCategorySelected: (SoundCategory) -> Unit,
    onSoundClick: (LocalizedSound) -> Unit,
    onVolumeChange: (soundId: String, volume: Float) -> Unit,
    onFrequencyChange: (soundId: String, frequency: EventFrequency) -> Unit,
    onRetry: () -> Unit,
) {
    JustRelaxTabScaffold(
        title = rememberGreeting(),
        actions = {
            IconButton(onClick = onOpenMySounds) {
                Icon(
                    Icons.Outlined.LibraryMusic,
                    stringResource(Res.string.action_my_sounds)
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(
                    Icons.Outlined.Settings,
                    stringResource(Res.string.action_settings)
                )
            }
        }
    ) { innerPadding ->
        LibraryContent(
            uiState = uiState,
            volumeOf = volumeOf,
            onCategorySelected = onCategorySelected,
            onSoundClick = onSoundClick,
            onVolumeChange = onVolumeChange,
            onFrequencyChange = onFrequencyChange,
            onRetry = onRetry,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun LibraryMessage.text(): String = when (this) {
    LibraryMessage.DownloadFailed -> stringResource(Res.string.download_failed)
    is LibraryMessage.Text -> value
}
