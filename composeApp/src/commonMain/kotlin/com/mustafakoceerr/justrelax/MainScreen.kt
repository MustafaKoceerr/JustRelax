package com.mustafakoceerr.justrelax

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxSnackbarHost
import com.mustafakoceerr.justrelax.core.ui.components.SaveMixDialog
import com.mustafakoceerr.justrelax.core.ui.controller.GlobalSnackbarController
import com.mustafakoceerr.justrelax.feature.player.PlayerViewModel
import com.mustafakoceerr.justrelax.feature.player.components.PlayerBottomBar
import com.mustafakoceerr.justrelax.feature.player.PlayerUiState
import com.mustafakoceerr.justrelax.core.ui.util.UserMessageEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.mustafakoceerr.justrelax.navigation.MainTab
import com.mustafakoceerr.justrelax.navigation.MainTabState
import com.mustafakoceerr.justrelax.tabs.MainTabContent
import com.mustafakoceerr.justrelax.tabs.icon
import com.mustafakoceerr.justrelax.tabs.title

/**
 * The main shell: a navigation bar on phones and a rail on wider windows (Material's navigation
 * suite), with the mini player at the bottom of the content. Tab ViewModels are scoped to this
 * destination, so they survive tab switches and rotation, and are cleared when the user leaves Main.
 */
@Composable
fun MainRoute(onOpenSettings: () -> Unit) {
    val playerViewModel = koinViewModel<PlayerViewModel>()
    val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = koinInject<GlobalSnackbarController>()

    UserMessageEffect(playerState.userMessage, playerViewModel::onMessageShown) {
        snackbarController.showSnackbar(it)
    }

    if (playerState.isSaveDialogVisible) {
        SaveMixDialog(
            isOpen = true,
            onDismiss = playerViewModel::dismissSaveDialog,
            onConfirm = playerViewModel::saveMix,
        )
    }

    MainScreenLayout(
        tabState = rememberSaveable(saver = MainTabState.Saver) { MainTabState() },
        playerState = playerState,
        onPlayPause = playerViewModel::togglePlayPause,
        onStopAll = playerViewModel::stopAll,
        onSaveMix = playerViewModel::openSaveDialog,
        onOpenSettings = onOpenSettings,
        snackbarHostState = snackbarController.hostState
    )
}

@Composable
private fun MainScreenLayout(
    tabState: MainTabState,
    playerState: PlayerUiState,
    onPlayPause: () -> Unit,
    onStopAll: () -> Unit,
    onSaveMix: () -> Unit,
    onOpenSettings: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val density = LocalDensity.current
    val isKeyboardOpen = WindowInsets.ime.getBottom(density) > 0
    // Keeps each tab's UI state (scroll position etc.) while another tab is shown.
    val tabStateHolder = rememberSaveableStateHolder()
    val navigationSuiteType =
        if (isKeyboardOpen) NavigationSuiteType.None
        else NavigationSuiteScaffoldDefaults.navigationSuiteType(currentWindowAdaptiveInfo())

    // Back on a tab other than Home goes to Home; on Home the system handles it (leaves the app).
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = tabState.interceptsBack,
        onBackCompleted = { tabState.onBack() },
    )

    JustRelaxBackground {
        NavigationSuiteScaffold(
            navigationItems = {
                MainTab.entries.forEach { tab ->
                    TabNavigationItem(
                        tab = tab,
                        isSelected = tab == tabState.selected,
                        navigationSuiteType = navigationSuiteType,
                        onClick = { tabState.select(tab) },
                    )
                }
            },
            navigationSuiteType = navigationSuiteType,
            navigationSuiteColors = NavigationSuiteDefaults.colors(
                shortNavigationBarContainerColor = MaterialTheme.colorScheme.surface,
                wideNavigationRailColors = WideNavigationRailDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ),
            containerColor = Color.Transparent,
        ) {
            Scaffold(
                contentWindowInsets = WindowInsets(0.dp),
                containerColor = Color.Transparent,
                snackbarHost = { JustRelaxSnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (!isKeyboardOpen) {
                        MiniPlayer(
                            playerState = playerState,
                            onPlayPause = onPlayPause,
                            onStopAll = onStopAll,
                            onSaveMix = onSaveMix,
                        )
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    AnimatedContent(
                        targetState = tabState.selected,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith
                                    fadeOut(animationSpec = tween(300))
                        },
                        label = "TabTransition"
                    ) { tab ->
                        tabStateHolder.SaveableStateProvider(tab.name) {
                            MainTabContent(
                                tab = tab,
                                onOpenSettings = onOpenSettings,
                                onSelectTab = tabState::select,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full width on phones; on wider windows it keeps a comfortable width at the bottom centre. Next to
 * a rail nothing below it takes the system navigation bar's inset, so it keeps clear of it itself.
 */
@Composable
private fun MiniPlayer(
    playerState: PlayerUiState,
    onPlayPause: () -> Unit,
    onStopAll: () -> Unit,
    onSaveMix: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = playerState.isVisible,
            modifier = Modifier.widthIn(max = MINI_PLAYER_MAX_WIDTH),
            enter = slideInVertically(
                animationSpec = tween(220),
                initialOffsetY = { fullHeight -> fullHeight }
            ) + fadeIn(animationSpec = tween(150)),
            exit = slideOutVertically(
                animationSpec = tween(220),
                targetOffsetY = { fullHeight -> fullHeight }
            ) + fadeOut(animationSpec = tween(150))
        ) {
            PlayerBottomBar(
                isVisible = true,
                activeIcons = playerState.activeSounds.map { it.icon },
                isPlaying = playerState.isPlaying,
                onPlayPauseClick = onPlayPause,
                onStopAllClick = onStopAll,
                onSaveClick = onSaveMix,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun TabNavigationItem(
    tab: MainTab,
    isSelected: Boolean,
    navigationSuiteType: NavigationSuiteType,
    onClick: () -> Unit,
) {
    val title = tab.title()

    NavigationSuiteItem(
        selected = isSelected,
        onClick = onClick,
        icon = { Icon(imageVector = tab.icon, contentDescription = title) },
        label = {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        },
        navigationSuiteType = navigationSuiteType,
    )
}

private val MINI_PLAYER_MAX_WIDTH = 640.dp
