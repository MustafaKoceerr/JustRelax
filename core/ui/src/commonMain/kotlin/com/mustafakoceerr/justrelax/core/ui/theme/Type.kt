package com.mustafakoceerr.justrelax.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mustafakoceerr.justrelax.core.ui.generated.resources.Res
import com.mustafakoceerr.justrelax.core.ui.generated.resources.nunito_bold
import com.mustafakoceerr.justrelax.core.ui.generated.resources.nunito_medium
import com.mustafakoceerr.justrelax.core.ui.generated.resources.nunito_regular
import com.mustafakoceerr.justrelax.core.ui.generated.resources.nunito_semibold
import org.jetbrains.compose.resources.Font

@Composable
fun getNunitoFontFamily(): FontFamily {
    val regular = Font(resource = Res.font.nunito_regular, weight = FontWeight.Normal)
    val medium = Font(resource = Res.font.nunito_medium, weight = FontWeight.Medium)
    val semiBold = Font(resource = Res.font.nunito_semibold, weight = FontWeight.SemiBold)
    val bold = Font(resource = Res.font.nunito_bold, weight = FontWeight.Bold)

    return remember(regular, medium, semiBold, bold) {
        FontFamily(regular, medium, semiBold, bold)
    }
}

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
