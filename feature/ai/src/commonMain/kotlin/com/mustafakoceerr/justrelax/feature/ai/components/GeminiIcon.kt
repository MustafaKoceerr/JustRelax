package com.mustafakoceerr.justrelax.feature.ai.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val GeminiStarIcon: ImageVector
    get() {
        if (_geminiStar != null) return _geminiStar!!
        _geminiStar = ImageVector.Builder(
            name = "GeminiStar",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black), // Rengi tint ile değiştireceğiz
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                // DÜZELTME: 'cubicTo' yerine 'curveTo' kullanıyoruz.
                // Mantık aynı: (x1, y1, x2, y2, x3, y3)

                moveTo(12.0f, 0.0f)

                // Sağ Üst Köşe
                curveTo(12.0f, 6.627f, 17.373f, 12.0f, 24.0f, 12.0f)

                // Sağ Alt Köşe
                curveTo(17.373f, 12.0f, 12.0f, 17.373f, 12.0f, 24.0f)

                // Sol Alt Köşe
                curveTo(12.0f, 17.373f, 6.627f, 12.0f, 0.0f, 12.0f)

                // Sol Üst Köşe
                curveTo(6.627f, 12.0f, 12.0f, 6.627f, 12.0f, 0.0f)

                close()
            }
        }.build()
        return _geminiStar!!
    }

private var _geminiStar: ImageVector? = null