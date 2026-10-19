package com.mustafakoceerr.justrelax.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mustafakoceerr.justrelax.core.ui.R

private val NunitoFontFamily = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_medium, FontWeight.Medium),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
)

@Composable
fun getNunitoFontFamily(): FontFamily = NunitoFontFamily

@Composable
fun getAppTypography(): Typography {
    val nunito = getNunitoFontFamily()

    return remember(nunito) {
        val base = Typography()
        // Headings are a little heavier and tighter than Material's defaults, so titles stand out
        // in the soft, rounded Nunito without needing more size.
        Typography(
            displayLarge = base.displayLarge.copy(fontFamily = nunito, fontWeight = FontWeight.SemiBold),
            displayMedium = base.displayMedium.copy(fontFamily = nunito, fontWeight = FontWeight.SemiBold),
            displaySmall = base.displaySmall.copy(fontFamily = nunito, fontWeight = FontWeight.SemiBold),
            headlineLarge = base.headlineLarge.copy(fontFamily = nunito, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.25).sp),
            headlineMedium = base.headlineMedium.copy(fontFamily = nunito, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.25).sp),
            headlineSmall = base.headlineSmall.copy(fontFamily = nunito, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.15).sp),
            titleLarge = base.titleLarge.copy(fontFamily = nunito, fontWeight = FontWeight.SemiBold),
            titleMedium = base.titleMedium.copy(fontFamily = nunito, fontWeight = FontWeight.SemiBold),
            titleSmall = base.titleSmall.copy(fontFamily = nunito, fontWeight = FontWeight.SemiBold),
            bodyLarge = base.bodyLarge.copy(fontFamily = nunito),
            bodyMedium = base.bodyMedium.copy(fontFamily = nunito),
            bodySmall = base.bodySmall.copy(fontFamily = nunito),
            labelLarge = base.labelLarge.copy(fontFamily = nunito, fontWeight = FontWeight.SemiBold),
            labelMedium = base.labelMedium.copy(fontFamily = nunito, fontWeight = FontWeight.SemiBold),
            labelSmall = base.labelSmall.copy(fontFamily = nunito, fontWeight = FontWeight.SemiBold),
        )
    }
}
