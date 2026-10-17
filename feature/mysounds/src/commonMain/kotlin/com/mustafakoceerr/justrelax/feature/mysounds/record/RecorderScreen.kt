package com.mustafakoceerr.justrelax.feature.mysounds.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mustafakoceerr.justrelax.core.media.ImportedAudio
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxBackground
import com.mustafakoceerr.justrelax.core.ui.components.JustRelaxTopBar
import com.mustafakoceerr.justrelax.core.ui.util.UserMessageEffect
import com.mustafakoceerr.justrelax.feature.mysounds.formatClipTime
import justrelax.feature.mysounds.generated.resources.Res
import justrelax.feature.mysounds.generated.resources.mysounds_back
import justrelax.feature.mysounds.generated.resources.mysounds_record_hint
import justrelax.feature.mysounds.generated.resources.mysounds_record_start
import justrelax.feature.mysounds.generated.resources.mysounds_record_stop
import justrelax.feature.mysounds.generated.resources.mysounds_record_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RecorderRoute(
    onBack: () -> Unit,
    onOpenEditor: (ImportedAudio) -> Unit,
    viewModel: RecorderViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val requestMicrophone = rememberMicrophonePermission(viewModel::onPermissionResult)

    UserMessageEffect(uiState.userMessage, viewModel::onMessageShown) { snackbarHostState.showSnackbar(it) }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onAppStopped() }
    LaunchedEffect(uiState.editorToOpen) {
        uiState.editorToOpen?.let {
            viewModel.onEditorOpened()
            onOpenEditor(it)
        }
    }

    RecorderScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = {
            viewModel.discard()
            onBack()
        },
        onStart = requestMicrophone,
        onStop = viewModel::stop,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecorderScreen(
    uiState: RecorderUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    JustRelaxBackground {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0.dp),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                JustRelaxTopBar(
                    title = stringResource(Res.string.mysounds_record_title),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.mysounds_back))
                        }
                    },
                )
            },
        ) { innerPadding ->
            Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth().navigationBarsPadding().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    Text(
                        text = "${formatClipTime(uiState.elapsedMs)} / ${formatClipTime(uiState.maxMs)}",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    LinearProgressIndicator(
                        progress = { uiState.level },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = stringResource(Res.string.mysounds_record_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Button(onClick = if (uiState.isRecording) onStop else onStart, modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            if (uiState.isRecording) Icons.Rounded.Stop else Icons.Rounded.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            stringResource(if (uiState.isRecording) Res.string.mysounds_record_stop else Res.string.mysounds_record_start),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }
    }
}
