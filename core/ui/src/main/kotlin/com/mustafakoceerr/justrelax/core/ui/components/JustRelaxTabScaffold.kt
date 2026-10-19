package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A tab of the main shell: transparent over the shell's background, with a top bar. The shell
 * already handles the system bars, so by default the content takes no insets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JustRelaxTabScaffold(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    contentWindowInsets: WindowInsets = WindowInsets(0.dp),
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentWindowInsets = contentWindowInsets,
        topBar = {
            JustRelaxTopBar(
                title = title,
                titleStyle = MaterialTheme.typography.headlineSmall,
                navigationIcon = navigationIcon,
                actions = actions,
            )
        },
        content = content,
    )
}
