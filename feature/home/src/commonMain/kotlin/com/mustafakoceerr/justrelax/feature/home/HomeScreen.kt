package com.mustafakoceerr.justrelax.feature.home

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.LocalizedSound
import com.mustafakoceerr.justrelax.core.ui.util.UserMessageEffect
import org.koin.compose.viewmodel.koinViewModel
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.home.components.HomeScreenContent
import justrelax.feature.home.generated.resources.Res
import justrelax.feature.home.generated.resources.action_settings
import justrelax.feature.home.generated.resources.home_screen_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun HomeRoute(
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = koinInject<GlobalSnackbarController>()

    UserMessageEffect(uiState.userMessage, viewModel::onMessageShown) { snackbarController.showSnackbar(it) }

    HomeScreen(
        uiState = uiState,
        onOpenSettings = onOpenSettings,
        onCategorySelected = viewModel::selectCategory,
        onSoundClick = viewModel::toggleSound,
        onVolumeChange = viewModel::changeVolume,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onOpenSettings: () -> Unit,
    onCategorySelected: (SoundCategory) -> Unit,
    onSoundClick: (LocalizedSound) -> Unit,
    onVolumeChange: (soundId: String, volume: Float) -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = Color.Transparent,
        topBar = {
            JustRelaxTopBar(
                title = stringResource(Res.string.home_screen_title),
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Outlined.Settings,
                            stringResource(Res.string.action_settings)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        HomeScreenContent(
            uiState = uiState,
            onCategorySelected = onCategorySelected,
            onSoundClick = onSoundClick,
            onVolumeChange = onVolumeChange,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
