package com.mustafakoceerr.justrelax.feature.saved

import com.mustafakoceerr.justrelax.core.model.SoundIcon
import com.mustafakoceerr.justrelax.core.model.soundscape.EventFrequency
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

/** A one-off message for the Saved tab; each platform turns it into its own text. */
sealed interface SavedMessage {
    /** The mix [name] was deleted; the message offers to undo it. */
    data class Deleted(val name: String) : SavedMessage
    data object PlayFailed : SavedMessage
}

data class SavedUiState(
    val isLoading: Boolean = true,
    val mixes: List<SavedMixItem> = emptyList(),
    val selected: SavedMixDetail? = null,
    /** The saved mix that is playing right now, if any. */
    val playingMixId: Long? = null,
    val message: SavedMessage? = null,
)
