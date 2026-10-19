package com.mustafakoceerr.justrelax.feature.saved

import com.mustafakoceerr.justrelax.core.model.SoundIcon
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import kotlin.time.Instant

data class SavedMixItem(
    val id: Long,
    val title: String,
    val date: Instant,
    val icons: List<SoundIcon>,
    /** In the app's language, in the mix's order. */
    val soundNames: List<String>,
)

/** A saved scene opened beside the list on wide windows. */
data class SavedMixDetail(
    val id: Long,
    val title: String,
    val date: Instant,
    val sounds: List<SavedSoundItem>,
)

/** [frequency] is null for a bed. */
data class SavedSoundItem(
    val name: String,
    val icon: SoundIcon,
    val volume: Float,
    val frequency: EventFrequency?,
)

/** A one-off snackbar message, optionally with an action (e.g. undo). */
data class SavedMessage(
    val text: UiText,
    val actionLabel: UiText? = null,
)

data class SavedUiState(
    val isLoading: Boolean = true,
    val mixes: List<SavedMixItem> = emptyList(),
    val selected: SavedMixDetail? = null,
    /** The saved mix that is playing right now, if any. */
    val playingMixId: Long? = null,
    val message: SavedMessage? = null,
)
