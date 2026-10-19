package com.mustafakoceerr.justrelax.tabs

import androidx.annotation.StringRes
import androidx.compose.material.icons.rounded.TravelExplore
import com.mustafakoceerr.justrelax.feature.freesound.FreesoundEditorRequest
import com.mustafakoceerr.justrelax.feature.freesound.FreesoundRoute
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.mustafakoceerr.justrelax.R
import com.mustafakoceerr.justrelax.navigation.MainTab
import com.mustafakoceerr.justrelax.feature.ai.AiRoute
import com.mustafakoceerr.justrelax.feature.library.LibraryRoute
import com.mustafakoceerr.justrelax.feature.mixer.MixerRoute
import com.mustafakoceerr.justrelax.feature.saved.SavedRoute

val MainTab.icon: ImageVector
    get() = when (this) {
        MainTab.HOME -> Icons.Rounded.GraphicEq
        MainTab.MIXER -> Icons.Rounded.Casino
        MainTab.DISCOVER -> Icons.Rounded.TravelExplore
        MainTab.AI -> Icons.Rounded.AutoAwesome
        MainTab.SAVED -> Icons.Rounded.Bookmark
    }

@get:StringRes
private val MainTab.titleRes: Int
    get() = when (this) {
        MainTab.HOME -> R.string.tab_home
        MainTab.AI -> R.string.tab_ai
        MainTab.SAVED -> R.string.tab_saved
        MainTab.MIXER -> R.string.tab_mixer
        MainTab.DISCOVER -> R.string.tab_discover
    }

@Composable
fun MainTab.title(): String = stringResource(titleRes)

/** Content of a bottom-bar tab. Navigation leaving the tab is passed in as callbacks. */
@Composable
fun MainTabContent(
    tab: MainTab,
    onOpenSettings: () -> Unit,
    onOpenMySounds: () -> Unit,
    onSelectTab: (MainTab) -> Unit,
    onSaveScene: (suggestedName: String?) -> Unit,
    onOpenSoundEditor: (FreesoundEditorRequest) -> Unit,
) {
    when (tab) {
        MainTab.HOME -> LibraryRoute(onOpenSettings = onOpenSettings, onOpenMySounds = onOpenMySounds)
        MainTab.AI -> AiRoute(onSaveScene = onSaveScene)
        MainTab.SAVED -> SavedRoute(onOpenMixer = { onSelectTab(MainTab.MIXER) })
        MainTab.MIXER -> MixerRoute(onSaveScene = { onSaveScene(null) })
        MainTab.DISCOVER -> FreesoundRoute(onOpenEditor = onOpenSoundEditor)
    }
}
