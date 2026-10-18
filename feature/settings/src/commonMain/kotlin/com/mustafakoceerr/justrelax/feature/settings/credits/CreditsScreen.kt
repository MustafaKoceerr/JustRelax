package com.mustafakoceerr.justrelax.feature.settings.credits

import com.mustafakoceerr.justrelax.core.ui.extensions.readableWidth
import com.mustafakoceerr.justrelax.core.ui.components.EmptyHint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxDetailScaffold
import com.mustafakoceerr.justrelax.core.ui.util.label
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.Res
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.credits_author
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.credits_empty
import com.mustafakoceerr.justrelax.feature.settings.generated.resources.credits_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreditsRoute(
    onBack: () -> Unit,
    viewModel: CreditsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CreditsScreen(uiState = uiState, onBack = onBack, onOpenSource = viewModel::openSource)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsScreen(
    uiState: CreditsUiState,
    onBack: () -> Unit,
    onOpenSource: (CreditItem) -> Unit,
) {
    JustRelaxDetailScaffold(
        title = stringResource(Res.string.credits_title),
        onBack = onBack,
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            if (!uiState.isLoading && uiState.credits.isEmpty()) {
                EmptyHint(stringResource(Res.string.credits_empty))
            } else {
                LazyColumn(
                    modifier = Modifier.readableWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    items(uiState.credits) { credit ->
                        ListItem(
                            headlineContent = { Text(credit.title) },
                            supportingContent = {
                                Text("${stringResource(Res.string.credits_author, credit.author)} · ${credit.license.label}")
                            },
                            trailingContent = {
                                IconButton(onClick = { onOpenSource(credit) }) {
                                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = credit.sourceUrl)
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        )
                    }
                }
            }
        }
    }
}

