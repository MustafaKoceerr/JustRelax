package com.mustafakoceerr.justrelax.feature.ai.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Google Gemini tarzı 4 köşeli yıldız ikonu.
 * Bu ikon AI özelliklerini temsil etmek için kullanılacak.
 * Singleton pattern ile oluşturulur (bir kez üretilir).
 */
val GeminiIcon: ImageVector
    get() {
        if (_geminiIcon != null) return _geminiIcon!!
        _geminiIcon = ImageVector.Builder(
            name = "GeminiIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black), // Tint ile rengi dışarıdan yönetilebilir
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Merkezden dışa doğru kavisli 4 köşe mantığı
                moveTo(12.0f, 0.0f)

                // Sağ Üst
                curveTo(12.0f, 6.627f, 17.373f, 12.0f, 24.0f, 12.0f)

                // Sağ Alt
                curveTo(17.373f, 12.0f, 12.0f, 17.373f, 12.0f, 24.0f)

                // Sol Alt
                curveTo(12.0f, 17.373f, 6.627f, 12.0f, 0.0f, 12.0f)

                // Sol Üst
                curveTo(6.627f, 12.0f, 12.0f, 6.627f, 12.0f, 0.0f)

                close()
            }
        }.build()
        return _geminiIcon!!
    }

private var _geminiIcon: ImageVector? = null