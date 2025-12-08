package com.mustafakoceerr.justrelax.feature.saved.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedMixUiModel

// --- YARDIMCI: Basit Veri Modeli ---


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipableSavedMixItem(
    mix: SavedMixUiModel,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    onShare: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
            onDelete()
            // Silindikten sonra state'i resetlemeye gerek yok, item listeden gidecek.
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = { SavedMixSwipeBackground(dismissState) },
        content = {
            SavedMixCard(
                title = mix.title,
                date = mix.date,
                soundCount = mix.icons.size,
                icons = mix.icons, // Artık List<String> (URL) gidiyor
                isPlaying = isPlaying,
                onPlayClick = onPlayClick,
                onRenameClick = onRename,
                onShareClick = onShare,
                onDeleteClick = onDelete
            )
        },
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = false
    )
}

