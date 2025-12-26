package com.mustafakoceerr.justrelax.feature.settings.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mustafakoceerr.justrelax.core.model.AppLanguage
import justrelax.feature.settings.generated.resources.Res
import justrelax.feature.settings.generated.resources.language_selection_title
import justrelax.feature.settings.generated.resources.language_system_default
import org.jetbrains.compose.resources.stringResource
@Composable
fun LanguageSelectionItem(
    language: AppLanguage,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val displayText = if (language == AppLanguage.SYSTEM) {
        stringResource(Res.string.language_system_default)
    } else {
        language.nativeName
    }

    // --- ANİMASYONLAR ---
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = tween(300),
        label = "LangContainerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(300),
        label = "LangContentColor"
    )
    // İYİLEŞTİRME: Kenarlık rengini anime ederek daha yumuşak bir geçiş sağlıyoruz.
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(300),
        label = "LangBorderColor"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        contentColor = contentColor,
        // Kenarlık her zaman var, sadece rengi değişiyor.
        border = BorderStroke(1.dp, borderColor),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.padding(vertical = 20.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayText,
                style = MaterialTheme.typography.titleMedium,
                // İYİLEŞTİRME: FontWeight'ı doğrudan atıyoruz.
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionBottomSheet(
    availableLanguages: List<AppLanguage>,
    currentLanguageCode: String,
    // DEĞİŞİKLİK GERİ ALINDI: Artık iki ayrı fonksiyon alıyor.
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.language_selection_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 48.dp)
            ) {
                items(availableLanguages) { language ->
                    LanguageSelectionItem(
                        language = language,
                        isSelected = language.code == currentLanguageCode,
                        onClick = {
                            onLanguageSelected(language)
                            onDismissRequest()
                        }
                    )
                }
            }
        }
    }
}