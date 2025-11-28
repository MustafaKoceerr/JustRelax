package com.mustafakoceerr.justrelax.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.mustafakoceerr.justrelax.core.generated.resources.Res
import com.mustafakoceerr.justrelax.core.generated.resources.nunito_bold
import com.mustafakoceerr.justrelax.core.generated.resources.nunito_medium
import com.mustafakoceerr.justrelax.core.generated.resources.nunito_regular
import com.mustafakoceerr.justrelax.core.generated.resources.nunito_semibold

import org.jetbrains.compose.resources.Font

// 1. Font Ailesini Tanımla
// @Composable olması gerekiyor çünkü kaynaklara erişim asenkron olabilir veya context gerektirebilir.
@Composable
fun getNunitoFontFamily(): FontFamily {
    return FontFamily(
        Font(resource = Res.font.nunito_regular, weight = FontWeight.Normal),
        Font(resource = Res.font.nunito_medium, weight = FontWeight.Medium),
        Font(resource = Res.font.nunito_semibold, weight = FontWeight.SemiBold),
        Font(resource = Res.font.nunito_bold, weight = FontWeight.Bold)
    )
}

// 2. Tipografiyi Oluştur
// Material 3 varsayılanlarını alıp, font ailesini değiştiriyoruz.
@Composable
fun getAppTypography(): Typography {
    val nunito = getNunitoFontFamily()

    return Typography(
        displayLarge = Typography().displayLarge.copy(fontFamily = nunito),
        displayMedium = Typography().displayMedium.copy(fontFamily = nunito),
        displaySmall = Typography().displaySmall.copy(fontFamily = nunito),

        headlineLarge = Typography().headlineLarge.copy(fontFamily = nunito),
        headlineMedium = Typography().headlineMedium.copy(fontFamily = nunito),
        headlineSmall = Typography().headlineSmall.copy(fontFamily = nunito),

        titleLarge = Typography().titleLarge.copy(fontFamily = nunito),
        titleMedium = Typography().titleMedium.copy(fontFamily = nunito),
        titleSmall = Typography().titleSmall.copy(fontFamily = nunito),

        bodyLarge = Typography().bodyLarge.copy(fontFamily = nunito),
        bodyMedium = Typography().bodyMedium.copy(fontFamily = nunito),
        bodySmall = Typography().bodySmall.copy(fontFamily = nunito),

        labelLarge = Typography().labelLarge.copy(fontFamily = nunito),
        labelMedium = Typography().labelMedium.copy(fontFamily = nunito),
        labelSmall = Typography().labelSmall.copy(fontFamily = nunito)
    )
}