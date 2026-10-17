package com.mustafakoceerr.justrelax.core.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue

enum class MainTab { HOME, TIMER, AI, SAVED, MIXER }

/**
 * Selected bottom-bar tab. Back on any tab other than [MainTab.HOME] returns to Home first;
 * on Home it is not handled, so the system can leave the app.
 */
@Stable
class MainTabState(initial: MainTab = MainTab.HOME) {

    var selected: MainTab by mutableStateOf(initial)
        private set

    val interceptsBack: Boolean
        get() = selected != MainTab.HOME

    fun select(tab: MainTab) {
        selected = tab
    }

    /** @return true if back was consumed by switching to Home. */
    fun onBack(): Boolean {
        if (!interceptsBack) return false
        selected = MainTab.HOME
        return true
    }

    companion object {
        val Saver: Saver<MainTabState, String> = Saver(
            save = { it.selected.name },
            restore = { MainTabState(MainTab.valueOf(it)) },
        )
    }
}
