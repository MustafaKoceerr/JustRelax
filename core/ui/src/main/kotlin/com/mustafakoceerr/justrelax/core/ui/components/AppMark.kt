package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.R
import androidx.compose.ui.res.painterResource

/**
 * The app's icon (a dusk sun sliced into layers by soft waves) in a circle, as the system splash
 * shows it: for the splash, settings and anywhere else the app names itself.
 */
@Composable
fun AppMark(modifier: Modifier = Modifier, size: Dp = 56.dp) {
    Image(
        painter = painterResource(R.drawable.app_mark),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(CircleShape),
    )
}
