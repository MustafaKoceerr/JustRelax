package com.mustafakoceerr.justrelax.core.common.util

import kotlin.math.roundToInt

fun Float.formatToOneDecimal(): String {
    // 1. 10 ile çarp -> 75.6f
    // 2. En yakın tam sayıya yuvarla -> 76
    // 3. 10.0'a bölerek ondalığı geri getir -> 7.6f
    val rounded = (this * 10).roundToInt() / 10.0f
    return rounded.toString()
}