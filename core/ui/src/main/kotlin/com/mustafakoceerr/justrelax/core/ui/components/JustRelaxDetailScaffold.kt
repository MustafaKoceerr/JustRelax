package com.mustafakoceerr.justrelax.core.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.ui.R
import androidx.compose.ui.res.stringResource

/**
 * A screen opened on top of the main shell: the app background, a top bar with a back button and
 * an optional snackbar and floating action button. The content gets the Scaffold's padding.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JustRelaxDetailScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    // Most screens draw edge to edge and handle insets in their content.
    contentWindowInsets: WindowInsets = WindowInsets(0.dp),
    content: @Composable (PaddingValues) -> Unit,
) {
    JustRelaxBackground {
        Scaffold(
            modifier = modifier,
            containerColor = Color.Transparent,
            contentWindowInsets = contentWindowInsets,
            snackbarHost = { snackbarHostState?.let { JustRelaxSnackbarHost(hostState = it) } },
            topBar = {
                JustRelaxTopBar(
                    title = title,
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.action_back))
                        }
                    },
                    actions = actions,
                )
            },
            floatingActionButton = floatingActionButton,
            content = content,
        )
    }
}
