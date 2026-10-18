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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.mustafakoceerr.justrelax.feature.scene.SceneViewModel
import com.mustafakoceerr.justrelax.feature.scene.components.MiniPlayerBar
import com.mustafakoceerr.justrelax.feature.scene.SceneUiState
import com.mustafakoceerr.justrelax.core.ui.util.UserMessageEffect
import com.mustafakoceerr.justrelax.core.ui.util.WindowWidthSize
import com.mustafakoceerr.justrelax.core.ui.util.rememberWindowSizeClass
import com.mustafakoceerr.justrelax.feature.scene.components.ScenePanel
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
import androidx.compose.runtime.LaunchedEffect
import com.mustafakoceerr.justrelax.core.analytics.AnalyticsHelper
import com.mustafakoceerr.justrelax.core.analytics.logScreenView

/**
 * The main shell: a navigation bar on phones and a rail on wider windows (Material's navigation
 * suite). The scene that is playing shows as a mini player at the bottom, which opens the scene
 * panel in a bottom sheet; on expanded windows the panel sits beside the library instead. Tab
 * ViewModels are scoped to this destination, so they survive tab switches and rotation, and are
 * cleared when the user leaves Main.
 */
@Composable
fun MainRoute(onOpenSettings: () -> Unit, onOpenMySounds: () -> Unit) {
    val sceneViewModel = koinViewModel<SceneViewModel>()
    val sceneState by sceneViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarController = koinInject<GlobalSnackbarController>()

    UserMessageEffect(sceneState.userMessage, sceneViewModel::onMessageShown) {
        snackbarController.showSnackbar(it)
    }

    if (sceneState.isSaveDialogVisible) {
        SaveMixDialog(
            isOpen = true,
            onDismiss = sceneViewModel::dismissSaveDialog,
            onConfirm = sceneViewModel::saveMix,
        )
    }

    MainScreenLayout(
        tabState = rememberSaveable(saver = MainTabState.Saver) { MainTabState() },
        sceneState = sceneState,
        onPlayPause = sceneViewModel::togglePlayPause,
        onStopAll = sceneViewModel::stopAll,
        onSaveMix = sceneViewModel::openSaveDialog,
        scenePanel = { modifier ->
            ScenePanel(
                state = sceneState,
                onPlayPause = sceneViewModel::togglePlayPause,
                onStopAll = sceneViewModel::stopAll,
                onSave = sceneViewModel::openSaveDialog,
                onVolumeChange = sceneViewModel::changeVolume,
                onFrequencyChange = sceneViewModel::changeFrequency,
                onRemove = sceneViewModel::remove,
                modifier = modifier,
            )
        },
        onOpenSettings = onOpenSettings,
        onOpenMySounds = onOpenMySounds,
        snackbarHostState = snackbarController.hostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenLayout(
    tabState: MainTabState,
    sceneState: SceneUiState,
    onPlayPause: () -> Unit,
    onStopAll: () -> Unit,
    onSaveMix: () -> Unit,
    scenePanel: @Composable (Modifier) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenMySounds: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val analytics = koinInject<AnalyticsHelper>()
    LaunchedEffect(tabState.selected) { analytics.logScreenView("main_${tabState.selected.name.lowercase()}") }

    val showsScenePane = rememberWindowSizeClass() == WindowWidthSize.EXPANDED && tabState.selected == MainTab.HOME
    var isSceneSheetOpen by rememberSaveable { mutableStateOf(false) }

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
                    if (!isKeyboardOpen && !showsScenePane) {
                        MiniPlayer(
                            sceneState = sceneState,
                            onPlayPause = onPlayPause,
                            onStopAll = onStopAll,
                            onSaveMix = onSaveMix,
                            onOpenScene = { isSceneSheetOpen = true },
                        )
                    }
                }
            ) { innerPadding ->
                Row(modifier = Modifier.padding(innerPadding)) {
                    AnimatedContent(
                        modifier = Modifier.weight(1f),
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
                                onOpenMySounds = onOpenMySounds,
                                onSelectTab = tabState::select,
                            )
                        }
                    }
                    if (showsScenePane) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier
                                .width(SCENE_PANE_WIDTH)
                                .fillMaxHeight()
                                .padding(8.dp)
                                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical + WindowInsetsSides.End)),
                        ) {
                            scenePanel(Modifier.padding(top = 16.dp))
                        }
                    }
                }
            }
        }
    }

    if (isSceneSheetOpen && !showsScenePane) {
        ModalBottomSheet(onDismissRequest = { isSceneSheetOpen = false }) {
            scenePanel(Modifier)
        }
    }
}

/**
 * Full width on phones; on wider windows it keeps a comfortable width at the bottom centre. Next to
 * a rail nothing below it takes the system navigation bar's inset, so it keeps clear of it itself.
 */
@Composable
private fun MiniPlayer(
    sceneState: SceneUiState,
    onPlayPause: () -> Unit,
    onStopAll: () -> Unit,
    onSaveMix: () -> Unit,
    onOpenScene: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = sceneState.isVisible,
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
            MiniPlayerBar(
                isVisible = true,
                activeIcons = sceneState.layers.map { it.icon },
                isPlaying = sceneState.isPlaying,
                onPlayPauseClick = onPlayPause,
                onStopAllClick = onStopAll,
                onSaveClick = onSaveMix,
                onOpenScene = onOpenScene,
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
private val SCENE_PANE_WIDTH = 380.dp
