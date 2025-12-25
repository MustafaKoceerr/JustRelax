package com.mustafakoceerr.justrelax.feature.onboarding.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import justrelax.feature.onboarding.generated.resources.Res
import justrelax.feature.onboarding.generated.resources.download_option_details
import org.jetbrains.compose.resources.stringResource

/**
 * SRP NOTU: Bu kart, ne indirildiğini bilmez.
 * Sadece bir başlık, ikon, ses sayısı ve boyut gibi verileri alıp
 * şık bir şekilde göstermekle sorumludur. Tıklanma olayını dışarıya bildirir.
 */
@Composable
fun DownloadOptionCard(
    icon: ImageVector,
    title: String,
    soundCount: Int,
    sizeInMb: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // --- 1. ANİMASYONLAR (Kenarlık dahil) ---
    val animationSpec = tween<androidx.compose.ui.graphics.Color>(300)

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = animationSpec,
        label = "CardContainerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        animationSpec = animationSpec,
        label = "CardContentColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        animationSpec = animationSpec,
        label = "CardBorderColor"
    )

    // --- 2. KART YAPISI ---
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp) // Yüksekliği sabit tutuyoruz
            .semantics {
                role = Role.RadioButton
                selected = isSelected
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(1.dp, borderColor),
        onClick = onClick
    ) {
        // --- 3. İÇERİK (Güvenli Yerleşim) ---
        // Column(fillMaxSize) yerine Box kullanmak, içeriğin ortalanmasını
        // daha güvenilir bir şekilde garanti eder ve layout döngülerini önler.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 4. LOKALİZASYON
                // Hardcoded string yerine formatlı kaynak kullanıyoruz.
                Text(
                    text = stringResource(Res.string.download_option_details, soundCount, sizeInMb),
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}