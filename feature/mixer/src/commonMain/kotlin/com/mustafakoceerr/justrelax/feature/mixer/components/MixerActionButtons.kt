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
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.extensions.rememberThrottledOnClick
import justrelax.feature.mixer.generated.resources.Res
import justrelax.feature.mixer.generated.resources.*
import org.jetbrains.compose.resources.stringResource

// import justrelax.composeapp.generated.resources.*
// import org.jetbrains.compose.resources.stringResource

/**
 * Yeni bir miks oluşturmak için kullanılan ana aksiyon butonu.
 * Yüklenme durumunu ve spam tıklamaları önleme (debounce) özelliğini destekler.
 */
@Composable
fun CreateMixButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val debouncedOnClick = rememberThrottledOnClick(throttleMs = 1000L, onClick = onClick)

    Button(
        onClick = debouncedOnClick,
        enabled = !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Crossfade(targetState = isLoading, label = "CreateButtonCrossfade") { loading ->
            if (loading) {
                // LoadingDots(...) // Senin yüklenme animasyonun
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

/**
 * Mevcut miksi kaydetmek için kullanılan ikincil aksiyon butonu.
 */
@Composable
fun SaveMixButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Icon(
            imageVector = Icons.Rounded.Save,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(Res.string.action_save_mix),
            style = MaterialTheme.typography.titleMedium
        )
    }
}