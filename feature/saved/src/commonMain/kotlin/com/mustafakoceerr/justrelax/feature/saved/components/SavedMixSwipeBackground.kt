package com.mustafakoceerr.justrelax.feature.saved.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import justrelax.feature.saved.generated.resources.Res
import justrelax.feature.saved.generated.resources.action_delete
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedMixSwipeBackground(
    dismissState: SwipeToDismissBoxState,
    modifier: Modifier = Modifier
) {
    val color =
        if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            Color.Transparent
        }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color, RoundedCornerShape(16.dp))
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = stringResource(
                    Res.string.action_delete
                ),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}