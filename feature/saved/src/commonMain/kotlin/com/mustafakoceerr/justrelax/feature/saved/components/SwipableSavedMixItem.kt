package com.mustafakoceerr.justrelax.feature.saved.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedContract

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipableSavedMixItem(
    mix: SavedContract.SavedMixUiModel,
    onPlayClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.targetValue) {
        if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) {
            onDelete()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            SavedMixSwipeBackground(
                dismissState = dismissState
            )
        },
        content = {
            SavedMixCard(
                title = mix.title,
                date = mix.date,
                soundCount = mix.icons.size,
                icons = mix.icons,
                onPlayClick = onPlayClick,
            )
        },
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = false,
        modifier = modifier
    )
}