package com.mustafakoceerr.justrelax.feature.mysounds.editor

import com.mustafakoceerr.justrelax.core.presentation.util.formatClipTime
import com.mustafakoceerr.justrelax.core.ui.extensions.readableWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakoceerr.justrelax.core.media.TrimProblem
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.SoundIcon
import com.mustafakoceerr.justrelax.core.model.SoundOrigin
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxDetailScaffold
import com.mustafakoceerr.justrelax.core.ui.R as CoreR
import com.mustafakoceerr.justrelax.core.ui.icon.imageVector
import com.mustafakoceerr.justrelax.core.ui.util.UserMessageEffect
import com.mustafakoceerr.justrelax.core.ui.util.title
import com.mustafakoceerr.justrelax.feature.mysounds.components.WaveformTrimmer
import com.mustafakoceerr.justrelax.feature.mysounds.ui.R
import com.mustafakoceerr.justrelax.feature.mysounds.labelRes
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SoundEditorRoute(
    sourcePath: String,
    suggestedName: String?,
    origin: SoundOrigin?,
    onBack: () -> Unit,
    viewModel: SoundEditorViewModel = koinViewModel(key = sourcePath) { parametersOf(sourcePath, suggestedName, origin) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    UserMessageEffect(uiState.userMessage?.text(), viewModel::onMessageShown) { snackbarHostState.showSnackbar(it) }
    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onBack() }

    SoundEditorScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onMoveStart = viewModel::moveStart,
        onMoveEnd = viewModel::moveEnd,
        onTogglePreview = viewModel::togglePreview,
        onSelectKind = viewModel::selectKind,
        onNameChange = viewModel::changeName,
        onSelectCategory = viewModel::selectCategory,
        onSelectIcon = viewModel::selectIcon,
        onSave = viewModel::save,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundEditorScreen(
    uiState: SoundEditorUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onMoveStart: (Long) -> Unit,
    onMoveEnd: (Long) -> Unit,
    onTogglePreview: () -> Unit,
    onSelectKind: (SoundKind) -> Unit,
    onNameChange: (String) -> Unit,
    onSelectCategory: (SoundCategory) -> Unit,
    onSelectIcon: (SoundIcon) -> Unit,
    onSave: () -> Unit,
) {
    JustRelaxDetailScaffold(
        title = stringResource(R.string.mysounds_editor_title),
        onBack = onBack,
        snackbarHostState = snackbarHostState,
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.padding(48.dp))
                uiState.loadFailed -> Text(
                    text = stringResource(R.string.mysounds_load_failed),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(24.dp),
                )
                // The scaffold is transparent over the app background, so text and icons need the surface colour.
                else -> CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                    EditorContent(
                        uiState, onMoveStart, onMoveEnd, onTogglePreview, onSelectKind,
                        onNameChange, onSelectCategory, onSelectIcon, onSave,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun EditorContent(
    uiState: SoundEditorUiState,
    onMoveStart: (Long) -> Unit,
    onMoveEnd: (Long) -> Unit,
    onTogglePreview: () -> Unit,
    onSelectKind: (SoundKind) -> Unit,
    onNameChange: (String) -> Unit,
    onSelectCategory: (SoundCategory) -> Unit,
    onSelectIcon: (SoundIcon) -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .readableWidth()
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            stringResource(R.string.mysounds_drag_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        WaveformTrimmer(uiState.peaks, uiState.sourceMs, uiState.range, onMoveStart, onMoveEnd)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(
                    R.string.mysounds_selection,
                    formatClipTime(uiState.range.startMs),
                    formatClipTime(uiState.range.endMs),
                    formatClipTime(uiState.range.lengthMs),
                ),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            FilledTonalButton(onClick = onTogglePreview) {
                Icon(if (uiState.isPreviewing) Icons.Rounded.Stop else Icons.Rounded.PlayArrow, contentDescription = null)
                Text(
                    stringResource(if (uiState.isPreviewing) CoreR.string.action_stop else CoreR.string.action_listen),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        uiState.trimProblem?.let {
            Text(stringResource(it.messageRes), color = MaterialTheme.colorScheme.error)
        }

        KindSelector(uiState.kind, uiState.allowedKinds, onSelectKind)

        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.mysounds_name)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )

        CategoryPicker(selected = uiState.category, onSelect = onSelectCategory)
        IconPicker(selected = uiState.shownIcon, onSelect = onSelectIcon)

        Button(onClick = onSave, enabled = uiState.canSave, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            if (uiState.isSaving) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.mysounds_save))
            }
        }
    }
}

// The pickers below are their own composables so they skip while the trim handles are dragged.

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryPicker(selected: SoundCategory?, onSelect: (SoundCategory) -> Unit) {
    Text(stringResource(R.string.mysounds_category), style = MaterialTheme.typography.titleSmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SoundCategory.entries.forEach { category ->
            FilterChip(
                selected = category == selected,
                onClick = { onSelect(category) },
                label = { Text(category.title()) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IconPicker(selected: SoundIcon?, onSelect: (SoundIcon) -> Unit) {
    Text(stringResource(R.string.mysounds_icon), style = MaterialTheme.typography.titleSmall)
    FlowRow {
        SoundIcon.entries.forEach { icon ->
            IconToggleButton(checked = icon == selected, onCheckedChange = { onSelect(icon) }) {
                Icon(icon.imageVector, contentDescription = icon.name.lowercase())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KindSelector(kind: SoundKind, allowed: Set<SoundKind>, onSelect: (SoundKind) -> Unit) {
    val kinds = listOf(SoundKind.BED, SoundKind.EVENT)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            kinds.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option == kind,
                    onClick = { onSelect(option) },
                    enabled = option in allowed,
                    shape = SegmentedButtonDefaults.itemShape(index, kinds.size),
                    label = {
                        Text(stringResource(option.labelRes))
                    },
                )
            }
        }
        val hint = when {
            SoundKind.BED !in allowed -> R.string.mysounds_kind_bed_unavailable
            kind == SoundKind.BED -> R.string.mysounds_kind_bed_hint
            else -> R.string.mysounds_kind_event_hint
        }
        Text(stringResource(hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@get:StringRes
private val TrimProblem.messageRes: Int
    get() = when (this) {
        TrimProblem.OUTSIDE_FILE -> R.string.mysounds_problem_outside_file
        TrimProblem.TOO_SHORT -> R.string.mysounds_problem_too_short
        TrimProblem.TOO_SHORT_FOR_BED -> R.string.mysounds_problem_too_short_for_bed
        TrimProblem.TOO_LONG_FOR_EVENT -> R.string.mysounds_problem_too_long_for_event
    }

@Composable
private fun SoundEditorMessage.text(): String = when (this) {
    SoundEditorMessage.SaveFailed -> stringResource(R.string.mysounds_save_failed)
}
