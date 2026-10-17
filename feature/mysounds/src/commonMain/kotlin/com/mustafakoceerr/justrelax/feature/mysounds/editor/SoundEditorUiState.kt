package com.mustafakoceerr.justrelax.feature.mysounds.editor

import com.mustafakoceerr.justrelax.core.media.TrimProblem
import com.mustafakoceerr.justrelax.core.media.TrimRange
import com.mustafakoceerr.justrelax.core.media.TrimRules
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.SoundIcon
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.core.ui.util.UiText

data class SoundEditorUiState(
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val peaks: List<Float> = emptyList(),
    val sourceMs: Long = 0,
    val range: TrimRange = TrimRange(0, 0),
    val kind: SoundKind = SoundKind.BED,
    val allowedKinds: Set<SoundKind> = emptySet(),
    val name: String = "",
    val category: SoundCategory? = null,
    /** Null until the user picks one; the category's icon is used then. */
    val icon: SoundIcon? = null,
    val isPreviewing: Boolean = false,
    val isSaving: Boolean = false,
    /** The sound is in the library; the screen closes. */
    val isSaved: Boolean = false,
    val userMessage: UiText? = null,
) {
    val trimProblem: TrimProblem?
        get() = if (isLoading || loadFailed) null else TrimRules.problem(range, kind, sourceMs)

    val shownIcon: SoundIcon? get() = icon ?: category?.defaultIcon

    val canSave: Boolean
        get() = !isLoading && !loadFailed && !isSaving && !isSaved &&
            name.isNotBlank() && category != null && trimProblem == null

    companion object {
        const val MAX_NAME_LENGTH = 40
    }
}
