package com.mustafakoceerr.justrelax.feature.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.feature.home.util.icon
import com.mustafakoceerr.justrelax.feature.home.util.titleRes
import org.jetbrains.compose.resources.stringResource

// Gerekli importlar...

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTabRow(
    categories: List<SoundCategory>,
    selectedCategory: SoundCategory,
    onCategorySelected: (SoundCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    PrimaryScrollableTabRow(
        selectedTabIndex = categories.indexOf(selectedCategory),
        modifier = modifier,
        edgePadding = 16.dp,
        containerColor = Color.Transparent,
        indicator = {},
        divider = {}
    ) {
        categories.forEach { category ->
            val isSelected = category == selectedCategory

            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                animationSpec = tween(durationMillis = 300),
                label = "TabBackgroundColor"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(durationMillis = 300),
                label = "TabContentColor"
            )

            Tab(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                content = {
                    CategoryTabContent(
                        category = category,
                        backgroundColor = backgroundColor,
                        contentColor = contentColor
                    )
                }
            )
        }
    }
}

@Composable
private fun CategoryTabContent(
    category: SoundCategory,
    backgroundColor: Color,
    contentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(category.titleRes),
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}