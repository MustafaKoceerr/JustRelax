package com.mustafakoceerr.justrelax.feature.mixer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.theme.JustRelaxTheme
import justrelax.feature.mixer.generated.resources.Res
import justrelax.feature.mixer.generated.resources.action_create_mix
import justrelax.feature.mixer.generated.resources.action_save_mix
import justrelax.feature.mixer.generated.resources.mix_count_selector_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MixNumberChip(
    number: Int,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onSelected,
        label = {
            Text(
                text = number.toString(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
        },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                Color.Transparent,
            borderWidth = 1.dp
        ),
        leadingIcon = null,
        modifier = modifier.size(50.dp)
    )
}

@Composable
fun MixCountSelector(
    selectedCount: Int,
    onCountSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.mix_count_selector_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(
                start = 16.dp,
                top = 16.dp,
                bottom = 16.dp
            )
        )

        val soundCounts = (2..8).toList()

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(soundCounts) { number ->
                MixNumberChip(
                    number = number,
                    isSelected = number == selectedCount,
                    onSelected = { onCountSelected(number) }
                )
            }
        }
    }
}

@Composable
fun CreateMixButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        if (isLoading) {
            LoadingDots(
                color = MaterialTheme.colorScheme.onPrimary,
                dotSize = 8.dp,
                travelDistance = 6.dp
            )
        } else {
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
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
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