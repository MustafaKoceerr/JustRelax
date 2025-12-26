package com.mustafakoceerr.justrelax.feature.saved.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.feature.saved.mvi.SavedMixUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipableSavedMixItem(
    mix: SavedMixUiModel,
    onPlayClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Deprecated olmayan modern kullanım: State'i doğrudan oluştur.
    val dismissState = rememberSwipeToDismissBoxState()

    // State'in hedefini (targetValue) izle. Kullanıcı parmağını kaldırdığında
    // bu değer değişir ve bu effect tetiklenir.
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
        enableDismissFromStartToEnd = true, // Sadece soldan sağa silme
        enableDismissFromEndToStart = false,
        modifier = modifier
    )
}