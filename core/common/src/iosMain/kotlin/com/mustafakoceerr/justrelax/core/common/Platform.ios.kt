package com.mustafakoceerr.justrelax.core.common

import platform.UIKit.UIDevice

actual fun platform(): String {
    return UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}