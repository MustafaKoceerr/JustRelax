package com.mustafakoceerr.justrelax.feature.settings.components

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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun DownloadAllCard(
    isDownloaded: Boolean,
    isDownloading: Boolean,
    progress: Float,
    onClick: () -> Unit
) {
    val backgroundColor =
        if (isDownloaded) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer
    val contentColor =
        if (isDownloaded) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(enabled = !isDownloaded && !isDownloaded){onClick()}
            .padding(16.dp)
    ){
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if(isDownloaded) Icons.Rounded.CheckCircle else
                    Icons.Rounded.CloudDownload,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = if (isDownloaded) stringResource(Res.string.download_all_completed_title) else stringResource(Res.string.download_all_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor
                )

                if (!isDownloaded && isDownloading){
                    Text(
                        text = stringResource(Res.string.download_all_subtitle_recommended),
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }
        }

        if (isDownloading){
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = {progress},
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = contentColor,
                trackColor = contentColor.copy(alpha = 0.3f),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    Res.string.download_progress,
                    (progress * 100).toInt()
                ),
                style = MaterialTheme.typography.labelSmall,
                color= contentColor,
                modifier = Modifier.align(Alignment.End)
                )
        }
    }
}