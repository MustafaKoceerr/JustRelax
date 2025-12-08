package com.mustafakoceerr.justrelax.core.common

import android.os.Build

actual fun platform(): String = "Android ${Build.VERSION.RELEASE}"
