package com.mustafakoceerr.justrelax.feature.saved.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.components.GenericEmptyState
import justrelax.feature.saved.generated.resources.Res
import justrelax.feature.saved.generated.resources.action_create_new_mix
import justrelax.feature.saved.generated.resources.saved_empty_description
import justrelax.feature.saved.generated.resources.saved_empty_image_cd
import justrelax.feature.saved.generated.resources.saved_empty_title
import justrelax.feature.saved.generated.resources.saved_empty_vector
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SavedMixesEmptyScreen(
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GenericEmptyState(
        modifier = modifier,
        visualContent = { EmptyStateVisual() },
        title = stringResource(Res.string.saved_empty_title),
        description = stringResource(Res.string.saved_empty_description),
        actionButtonText = stringResource(Res.string.action_create_new_mix),
        onActionClick = onCreateClick
    )
}

@Composable
private fun EmptyStateVisual() {
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.saved_empty_vector),
            contentDescription = stringResource(Res.string.saved_empty_image_cd),
            modifier = Modifier
                .height(220.dp)
                .fillMaxWidth()
                .graphicsLayer { translationY = offsetY },
            contentScale = ContentScale.Fit
        )
    }
}