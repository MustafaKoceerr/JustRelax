package com.mustafakoceerr.justrelax.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
        Typography(
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
}