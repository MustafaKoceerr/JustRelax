package com.mustafakoceerr.justrelax.feature.mixer.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.components.LoadingDots
import com.mustafakoceerr.justrelax.core.ui.extensions.rememberThrottledOnClick
import justrelax.feature.mixer.generated.resources.Res
import justrelax.feature.mixer.generated.resources.action_create_mix
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateMixButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val debouncedOnClick = rememberThrottledOnClick(throttleMs = 800L, onClick = onClick)

    Button(
        onClick = debouncedOnClick,
        enabled = !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
    ) {
        Crossfade(targetState = isLoading, label = "CreateButtonCrossfade") { loading ->
            if (loading) {
                LoadingDots(
                    color = MaterialTheme.colorScheme.onPrimary,
                    dotSize = 8.dp
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.action_create_mix),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

