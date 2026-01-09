package com.mustafakoceerr.justrelax.feature.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import justrelax.feature.settings.generated.resources.Res
import justrelax.feature.settings.generated.resources.download_all_completed_title
import justrelax.feature.settings.generated.resources.download_all_subtitle_recommended
import justrelax.feature.settings.generated.resources.download_all_title
import justrelax.feature.settings.generated.resources.download_progress
import org.jetbrains.compose.resources.stringResource

private enum class DownloadCardState { IDLE, DOWNLOADING, COMPLETED }

@Composable
fun DownloadAllCard(
    isDownloaded: Boolean,
    isDownloading: Boolean,
    progress: Float,
    onClick: () -> Unit
) {
    val cardState = remember(isDownloaded, isDownloading) {
        when {
            isDownloaded -> DownloadCardState.COMPLETED
            isDownloading -> DownloadCardState.DOWNLOADING
            else -> DownloadCardState.IDLE
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (cardState == DownloadCardState.COMPLETED) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.tertiaryContainer,
        animationSpec = tween(durationMillis = 400),
        label = "CardBackgroundColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (cardState == DownloadCardState.COMPLETED) MaterialTheme.colorScheme.onSecondaryContainer
        else MaterialTheme.colorScheme.onTertiaryContainer,
        animationSpec = tween(durationMillis = 400),
        label = "CardContentColor"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(enabled = cardState == DownloadCardState.IDLE, onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (cardState == DownloadCardState.COMPLETED) Icons.Rounded.CheckCircle
                else Icons.Rounded.CloudDownload,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (cardState == DownloadCardState.COMPLETED) stringResource(Res.string.download_all_completed_title)
                    else stringResource(Res.string.download_all_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor
                )

                if (cardState != DownloadCardState.COMPLETED) {
                    Text(
                        text = stringResource(Res.string.download_all_subtitle_recommended),
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = cardState == DownloadCardState.DOWNLOADING,
            enter = expandVertically(animationSpec = tween(400)),
            exit = shrinkVertically(animationSpec = tween(400))
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = contentColor,
                    trackColor = contentColor.copy(alpha = 0.3f),
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.download_progress, (progress * 100).toInt()),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}