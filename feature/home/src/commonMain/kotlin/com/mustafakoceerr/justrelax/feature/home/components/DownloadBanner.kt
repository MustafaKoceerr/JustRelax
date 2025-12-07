package com.mustafakoceerr.justrelax.feature.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DownloadBanner(
    isVisible: Boolean,
    isDownloading: Boolean,
    downloadProgress: Float, // 0.0f - 1.0f arası
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Görünürlük Animasyonu (Açılış/Kapanış)
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier.padding(16.dp) // Kenarlardan boşluk
    ) {
        // Kart Tasarımı
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                // Bilgilendirme olduğu için Secondary veya Tertiary Container çok yakışır
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            elevation = CardDefaults.cardElevation(0.dp) // Banner genelde düz olur
        ) {
            // İçerik Değişimi Animasyonu (Soru <-> Progress)
            AnimatedContent(
                targetState = isDownloading,
                label = "BannerContent"
            ) { downloading ->
                if (downloading) {
                    // --- DURUM 2: İNDİRİLİYOR ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Dönen ufak bir indirme ikonu veya sabit ikon
                        Icon(Icons.Rounded.Download, null)

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sesler indiriliyor... %${(downloadProgress * 100).toInt()}",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap = StrokeCap.Round // Yuvarlak uçlu bar
                            )
                        }
                    }
                } else {
                    // --- DURUM 1: SORU ---
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CloudDownload, null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Tüm sesleri şimdi indirelim mi?",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Kesintisiz deneyim için tüm kütüphaneyi cihazına kaydedebilirsin.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 36.dp) // İkon hizasından başlasın
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Butonlar (Sağa Yaslı)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Reddet (Text Button - Düşük Vurgu)
                            TextButton(onClick = onDismiss) {
                                Text("Daha Sonra")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Onayla (Filled Tonal veya Filled - Yüksek Vurgu)
                            Button(
                                onClick = onConfirm,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(Icons.Rounded.Check, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("İndir")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(
    name = "DownloadBanner – Soru",
    showBackground = true
)
@Composable
fun DownloadBannerQuestionPreview() {
    MaterialTheme {
        DownloadBanner(
            isVisible = true,
            isDownloading = false,
            downloadProgress = 0f,
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview(
    name = "DownloadBanner – İndirme Devam Ediyor",
    showBackground = true
)
@Composable
fun DownloadBannerDownloadingPreview() {
    MaterialTheme {
        DownloadBanner(
            isVisible = true,
            isDownloading = true,
            downloadProgress = 0.45f,
            onConfirm = {},
            onDismiss = {}
        )
    }
}